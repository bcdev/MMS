import os
import subprocess
import sys

from status_codes import StatusCodes


class JasminJobMonitor:
    @staticmethod
    def main():
        pm_request = ""

        for i in range(1, len(sys.argv)):
            pm_request += sys.argv[i]
            pm_request += " "

        # remove last blank
        pm_request = pm_request[:-1]

        monitor = JasminJobMonitor()
        return monitor.run(pm_request)

    def run(self, pm_request):
        try:
            scheduler_interface = self._create_scheduler_interface()
            watch_dict = self._parse_watch_dict(pm_request)

            process_output = scheduler_interface.call_job_status()
            job_status_dict = scheduler_interface.parse_jobs_call(process_output)

            resolved, check_log = self._resolve_jobs(watch_dict, job_status_dict)
            log_resolved = scheduler_interface.resolve_status_from_log(check_log)

            resolved.update(log_resolved)

            self._format_and_write(resolved, sys.stdout)

            return 0
        except (RuntimeError, ValueError) as e:
            sys.stderr.write(str(e))
            return 1

    @staticmethod
    def _create_scheduler_interface():
        if "SCHEDULER" in os.environ:
            scheduler_name = os.environ["SCHEDULER"]
            if scheduler_name == "LSF":
                return  LSFInterface()
            elif scheduler_name == "SLURM":
                return SLURMInterface()
            elif scheduler_name == "SLURM_2":
                return SLURMInterface()
            else:
                raise ValueError("Environment variable 'SCHEDULER' invalid" + scheduler_name)
        else:
            raise ValueError("Environment variable 'SCHEDULER' is not set")

    @staticmethod
    def _parse_watch_dict(pm_request):
        watch_dict = {}
        tokens = pm_request.split(" ")
        for token in tokens:
            if len(token) == 0:
                continue

            idx = token.find("_")
            if idx < 0:
                raise ValueError("Illegal ID encoding: " + token)

            job_id = token[:idx]
            jobname = token[idx + 1:]
            watch_dict.update({job_id: jobname})

        return watch_dict

    @staticmethod
    def _resolve_jobs(watch_dict, job_status_dict):
        resolved_dict = {}
        check_log_dict = {}
        for key, value in watch_dict.items():
            jobname = value
            if key in job_status_dict:
                status = job_status_dict[key]
                resolved_dict.update({key: [jobname, status, ""]})
                pass
            else:
                check_log_dict.update({key: jobname})

        return resolved_dict, check_log_dict

    @staticmethod
    def _get_log_dir():
        if "PM_LOG_DIR" in os.environ:
            log_dir = os.environ["PM_LOG_DIR"]
            if not os.path.isdir(log_dir):
                raise ValueError("log directory does not exist: " + log_dir)
            return log_dir
        else:
            raise RuntimeError("Missing environment variable 'PM_LOG_DIR'")

    @staticmethod
    def _format_and_write(results, stream):
        if (len(results) == 0):
            stream.flush()
            return

        key_list = list(results.keys())

        num_items = len(key_list)
        for i in range(0, num_items):
            key = key_list[i]
            result = results[key]
            stream.write(key + "_" + result[0] + ",")
            stream.write(result[1].value + ",")
            stream.write(result[2])
            stream.write("\n")

        stream.flush()


class LSFInterface:

    def call_job_status(self):
        completed_process = subprocess.run(["bjobs"], stdout=subprocess.PIPE, stderr=subprocess.PIPE)
        if completed_process.returncode != 0:
            sys.stderr.write(completed_process.stderr.decode("utf-8"))
            sys.stderr.flush()
            raise RuntimeError("Failed to request job status")
        process_output = completed_process.stdout.decode("utf-8")
        return process_output

    def parse_jobs_call(self, message):
        job_status_dict = {}

        lines = message.split("\n")
        for i in range(1, len(lines)):
            if len(lines[i]) == 0:
                continue

            id_status = self._extract_id_and_status(lines[i])
            job_status_dict.update(id_status)

        return job_status_dict

    def _extract_id_and_status(self, line):
        tokens = line.split()

        if len(tokens) < 8:
            raise ValueError("unable to handle 'bjobs' result: " + line)

        status_code = self._status_to_enum(tokens[2])
        return {tokens[0]: status_code}

    @staticmethod
    def _status_to_enum(status):
        if "RUN" == status:
            return StatusCodes.RUNNING
        elif status in ["PEND", "PROV", "WAIT"]:
            return StatusCodes.SCHEDULED
        elif "DONE" == status:
            return StatusCodes.DONE
        elif "EXIT" == status:
            return StatusCodes.FAILED
        elif "UNKWN" == status:
            return StatusCodes.UNKNOWN
        elif status in ["PSUSP", "USUSP", "SSUSP", "ZOMBI"]:
            return StatusCodes.DROPPED

        raise ValueError("unsupported status code: " + status)

    def resolve_status_from_log(self, check_log_dict):
        resolved_dict = {}
        log_dir = JasminJobMonitor._get_log_dir()

        for key, value in check_log_dict.items():
            logfile = os.path.join(log_dir, value + ".out")
            if not os.path.isfile(logfile):
                resolved_dict.update({key: [value, StatusCodes.DROPPED, "log file for job does not exist: " + logfile]})
                continue

            with open(logfile) as f:
                log_content = f.read()
                if "Successfully completed." in log_content:
                    resolved_dict.update({key: [value, StatusCodes.DONE, ""]})
                else:
                    resolved_dict.update({key: [value, StatusCodes.FAILED, "Check log file for details: " + logfile]})

        return resolved_dict

class SLURMInterface:

    user_name = None
    scheduler_name = None

    def __init__(self):
        if "MMS_USER" in os.environ:
            self.user_name = os.environ["MMS_USER"]
            if "SCHEDULER" in os.environ:
                self.scheduler_name = os.environ["SCHEDULER"]
        else:
            raise RuntimeError("Missing environment variable 'MMS_USER'")

    def call_job_status(self):
        completed_process = subprocess.run(['squeue',  '--users=' + self.user_name, '--states=all'], stdout=subprocess.PIPE, stderr=subprocess.PIPE)
        if completed_process.returncode != 0:
            sys.stderr.write(completed_process.stderr.decode("utf-8"))
            sys.stderr.flush()
            raise RuntimeError("Failed to request job status")
        process_output = completed_process.stdout.decode("utf-8")
        return process_output

    def parse_jobs_call(self, message):
        job_status_dict = {}

        lines = message.split("\n")
        for i in range(1, len(lines)):
            if len(lines[i]) == 0:
                continue

            id_status = self._extract_id_and_status(lines[i])
            job_status_dict.update(id_status)

        return job_status_dict

    def _extract_id_and_status(self, line):
        tokens = line.split()

        # seven tokens are used for canceled jobs, 8 is standard tb 2022-09-21
        if len(tokens) < 7:
            raise ValueError("unable to handle 'squeue' result: " + line)

        if self.scheduler_name == "SLURM_2":
            status_token = tokens[7]
        else:
            status_token = tokens[4]

        status_code = self._status_to_enum(status_token)
        return {tokens[0]: status_code}

    @staticmethod
    def _status_to_enum(status):
        if status in ["R", "CG", "SO"]:
            return StatusCodes.RUNNING
        elif status in ["CF", "PD", "RD", "RF", "RH", "RQ", "RS", "RV"]:
            return StatusCodes.SCHEDULED
        elif status in ["CD", "SE"]:
            return StatusCodes.DONE
        elif status in ["BF", "DL", "F", "NF", "OOM", "TO"]:
            return StatusCodes.FAILED
        elif status in ["CA", "PR", "SI", "ST", "S"]:
            return StatusCodes.DROPPED

        raise ValueError("unsupported status code: " + status)

    def resolve_status_from_log(self, check_log_dict):
        resolved_dict = {}
        log_dir = JasminJobMonitor._get_log_dir()

        for key, value in check_log_dict.items():
            logfile = os.path.join(log_dir, value + ".out")
            if not os.path.isfile(logfile):
                resolved_dict.update({key: [value, StatusCodes.DROPPED, "log file for job does not exist: " + logfile]})
            else:
                resolved_dict.update({key: [value, StatusCodes.DONE, ""]})

        return resolved_dict

if __name__ == "__main__":
    sys.exit(JasminJobMonitor.main())
