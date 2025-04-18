import os
import io
import unittest

from jasmin_job_monitor import JasminJobMonitor, LSFInterface, SLURMInterface
from status_codes import StatusCodes


class JasminJobMonitorTest(unittest.TestCase):

    def test_create_scheduler_interface_no_environ(self):
        try:
            JasminJobMonitor._create_scheduler_interface()
            self.fail("ValueError expected")
        except(ValueError):
            pass

    def test_create_scheduler_interface_LSF(self):
        try:
            os.environ["SCHEDULER"] = "LSF"

            scheduler_interface = JasminJobMonitor._create_scheduler_interface()
            self.assertIsInstance(scheduler_interface, LSFInterface)
        finally:
            del os.environ["SCHEDULER"]

    def test_create_scheduler_interface_SLURM(self):
        try:
            os.environ["SCHEDULER"] = "SLURM"
            os.environ["MMS_USER"] = "HarryPotter"

            scheduler_interface = JasminJobMonitor._create_scheduler_interface()
            self.assertIsInstance(scheduler_interface, SLURMInterface)
        finally:
            del os.environ["SCHEDULER"]
            del os.environ["MMS_USER"]

    def test_create_scheduler_interface_nonsense(self):
        try:
            os.environ["SCHEDULER"] = "heffalump"

            JasminJobMonitor._create_scheduler_interface()
            self.fail("ValueError expected")
        except(ValueError):
            pass
        finally:
            del os.environ["SCHEDULER"]

    def test_parse_job_status_SLURM(self):
        output = "             JOBID PARTITION     NAME     USER ST       TIME  NODES NODELIST(REASON)\n" \
                 "8158926 short-ser the_job. tblock01  R       1:46      1 host143\n" \
                 "14839281 short-ser ingest-s tblock01  F       0:57      1 (NonZeroExitCode)"

        try:
            os.environ["MMS_USER"] = "HarryPotter"

            scheduler = SLURMInterface()
            job_status_dict = scheduler.parse_jobs_call(output)
            self.assertEqual(2, len(job_status_dict))
            self.assertEqual(StatusCodes.RUNNING, job_status_dict["8158926"])
            self.assertEqual(StatusCodes.FAILED, job_status_dict["14839281"])
        finally:
            del os.environ["MMS_USER"]

    def test_parse_job_status_SLURM_2(self):
        output = "   JOBID PARTIT       QOS                 NAME       USER NODE  CPUS ST         TIME    TIME_LEFT PRIORITY NODELIST(REASON)\n" \
                 "8466477 standa     short ingest-slstr-s3a-uor   tblock01    1     1  R         0:37      3:59:23    77911 (NonZeroExitCode)\n" \
               "8466478 standa     short ingest-slstr-s3a-uor   tblock01    1     1  F         0:37      3:59:23    77911 (NonZeroExitCode)"


        try:
            os.environ["MMS_USER"] = "HarryPotter"
            os.environ["SCHEDULER"] = "SLURM_2"

            scheduler = SLURMInterface()
            job_status_dict = scheduler.parse_jobs_call(output)
            self.assertEqual(2, len(job_status_dict))
            self.assertEqual(StatusCodes.RUNNING, job_status_dict["8466477"])
            self.assertEqual(StatusCodes.FAILED, job_status_dict["8466478"])
        finally:
            del os.environ["MMS_USER"]
            del os.environ["SCHEDULER"]

    def test_status_to_enum_LSF(self):
        scheduler = LSFInterface()

        self.assertEqual(StatusCodes.RUNNING, scheduler._status_to_enum("RUN"))

        self.assertEqual(StatusCodes.SCHEDULED, scheduler._status_to_enum("PEND"))
        self.assertEqual(StatusCodes.SCHEDULED, scheduler._status_to_enum("PROV"))
        self.assertEqual(StatusCodes.SCHEDULED, scheduler._status_to_enum("WAIT"))

        self.assertEqual(StatusCodes.FAILED, scheduler._status_to_enum("EXIT"))

        self.assertEqual(StatusCodes.DROPPED, scheduler._status_to_enum("PSUSP"))
        self.assertEqual(StatusCodes.DROPPED, scheduler._status_to_enum("USUSP"))
        self.assertEqual(StatusCodes.DROPPED, scheduler._status_to_enum("SSUSP"))
        self.assertEqual(StatusCodes.DROPPED, scheduler._status_to_enum("ZOMBI"))

        self.assertEqual(StatusCodes.DONE, scheduler._status_to_enum("DONE"))

        self.assertEqual(StatusCodes.UNKNOWN, scheduler._status_to_enum("UNKWN"))

    def test_status_to_enum_SLURM(self):
        try:
            os.environ["MMS_USER"] = "RonWeasley"

            scheduler = SLURMInterface()
            self.assertEqual(StatusCodes.RUNNING, scheduler._status_to_enum("R"))
            self.assertEqual(StatusCodes.RUNNING, scheduler._status_to_enum("CG"))
            self.assertEqual(StatusCodes.RUNNING, scheduler._status_to_enum("SO"))

            self.assertEqual(StatusCodes.SCHEDULED, scheduler._status_to_enum("CF"))
            self.assertEqual(StatusCodes.SCHEDULED, scheduler._status_to_enum("PD"))
            self.assertEqual(StatusCodes.SCHEDULED, scheduler._status_to_enum("RD"))
            self.assertEqual(StatusCodes.SCHEDULED, scheduler._status_to_enum("RF"))
            self.assertEqual(StatusCodes.SCHEDULED, scheduler._status_to_enum("RH"))
            self.assertEqual(StatusCodes.SCHEDULED, scheduler._status_to_enum("RQ"))
            self.assertEqual(StatusCodes.SCHEDULED, scheduler._status_to_enum("RS"))
            self.assertEqual(StatusCodes.SCHEDULED, scheduler._status_to_enum("RV"))

            self.assertEqual(StatusCodes.FAILED, scheduler._status_to_enum("BF"))
            self.assertEqual(StatusCodes.FAILED, scheduler._status_to_enum("DL"))
            self.assertEqual(StatusCodes.FAILED, scheduler._status_to_enum("F"))
            self.assertEqual(StatusCodes.FAILED, scheduler._status_to_enum("NF"))
            self.assertEqual(StatusCodes.FAILED, scheduler._status_to_enum("OOM"))
            self.assertEqual(StatusCodes.FAILED, scheduler._status_to_enum("TO"))

            self.assertEqual(StatusCodes.DROPPED, scheduler._status_to_enum("CA"))
            self.assertEqual(StatusCodes.DROPPED, scheduler._status_to_enum("PR"))
            self.assertEqual(StatusCodes.DROPPED, scheduler._status_to_enum("SI"))
            self.assertEqual(StatusCodes.DROPPED, scheduler._status_to_enum("ST"))
            self.assertEqual(StatusCodes.DROPPED, scheduler._status_to_enum("S"))

            self.assertEqual(StatusCodes.DONE, scheduler._status_to_enum("CD"))
            self.assertEqual(StatusCodes.DONE, scheduler._status_to_enum("SE"))
        finally:
            del os.environ["MMS_USER"]

    def test_parse_watch_dict(self):
        cmd_line = "14837827_ingest-slstr-s3a-nt-v01-2020-001-2020-001 14837826_ingest-slstr-s3a-nt-v01-2020-002-2020-002 14837829_ingest-slstr-s3a-nt-v01-2020-003-2020-003 14837830_ingest-slstr-s3a-nt-v01-2020-004-2020-004 14837833_ingest-slstr-s3a-nt-v01-2020-005-2020-005 14837834_ingest-slstr-s3a-nt-v01-2020-006-2020-006 14837836_ingest-slstr-s3a-nt-v01-2020-007-2020-007"

        watch_dict = JasminJobMonitor._parse_watch_dict(cmd_line)
        self.assertEqual(7, len(watch_dict))
        self.assertEqual("ingest-slstr-s3a-nt-v01-2020-001-2020-001", watch_dict["14837827"])
        self.assertEqual("ingest-slstr-s3a-nt-v01-2020-005-2020-005", watch_dict["14837833"])

    def test_resolve_jobs_two_empty_dicts(self):
        resolved, check_log = JasminJobMonitor._resolve_jobs({},{})
        self.assertEqual(0, len(resolved))
        self.assertEqual(0, len(check_log))

    def test_resolve_jobs_empty_status_dict(self):
        resolved, check_log = JasminJobMonitor._resolve_jobs({12: "Willem"},{})
        self.assertEqual(0, len(resolved))
        self.assertEqual(1, len(check_log))
        self.assertEqual("Willem", check_log[12])

    def test_resolve_jobs(self):
        resolved, check_log = JasminJobMonitor._resolve_jobs({12: "Willem"},{12: StatusCodes.DONE})
        self.assertEqual(1, len(resolved))
        self.assertEqual(["Willem", StatusCodes.DONE, ""], resolved[12])
        self.assertEqual(0, len(check_log))

    def test_resolve_jobs_both_dicts(self):
        resolved, check_log = JasminJobMonitor._resolve_jobs({12: "Willem", 13: "Agathe", 14: "Sumse"},{12: StatusCodes.DONE, 14: StatusCodes.DONE})
        self.assertEqual(2, len(resolved))
        self.assertEqual(["Willem", StatusCodes.DONE, ""], resolved[12])
        self.assertEqual(["Sumse", StatusCodes.DONE, ""], resolved[14])
        self.assertEqual(1, len(check_log))
        self.assertEqual("Agathe", check_log[13])

    def test_format_and_write_empty(self):
        io_stream = io.StringIO()
        JasminJobMonitor._format_and_write([], io_stream)

        self.assertEqual("", io_stream.read())

    def test_format_and_write_one(self):
        io_stream = io.StringIO()
        JasminJobMonitor._format_and_write({"236": ["Willem", StatusCodes.DONE, ""]}, io_stream)

        io_stream.seek(0)
        self.assertEqual("236_Willem,DONE,\n", io_stream.read())

    def test_format_and_write_three(self):
        io_stream = io.StringIO()
        JasminJobMonitor._format_and_write({"237": ["Hans", StatusCodes.DONE, ""],
                                            "238": ["Jana", StatusCodes.DONE, ""],
                                            "239": ["Hinni", StatusCodes.RUNNING, ""]},
                                           io_stream)

        io_stream.seek(0)
        self.assertEqual("237_Hans,DONE,\n"
                         "238_Jana,DONE,\n"
                         "239_Hinni,RUNNING,\n", io_stream.read())
