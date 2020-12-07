package com.bc.fiduceo.post.plugin.era5;

import com.bc.fiduceo.FiduceoConstants;
import ucar.ma2.DataType;
import ucar.nc2.Dimension;
import ucar.nc2.NetcdfFile;
import ucar.nc2.NetcdfFileWriter;
import ucar.nc2.Variable;

import java.util.*;

class MatchupFields {

    private Map<String, TemplateVariable> variables;

    void prepare(MatchupFieldsConfiguration matchupFieldsConfig, NetcdfFile reader, NetcdfFileWriter writer) {
        matchupFieldsConfig.verify();

        final List<Dimension> dimensions = getDimensions(matchupFieldsConfig, writer, reader);

        variables = getVariables(matchupFieldsConfig);
        final Collection<TemplateVariable> values = variables.values();
        for (TemplateVariable template : values) {
            final Variable variable = writer.addVariable(template.getName(), DataType.FLOAT, dimensions);
            VariableUtils.addAttributes(template, variable);
        }
    }

    void compute() {

    }

    // package access for testing purpose only tb 2020-12-02
    List<Dimension> getDimensions(MatchupFieldsConfiguration matchupFieldsConfig, NetcdfFileWriter writer, NetcdfFile reader) {
        final ArrayList<Dimension> dimensions = new ArrayList<>();

        final Dimension matchupDim = reader.findDimension(FiduceoConstants.MATCHUP_COUNT);
        dimensions.add(matchupDim);

        final int time_steps_past = matchupFieldsConfig.getTime_steps_past();
        final int time_steps_future = matchupFieldsConfig.getTime_steps_future();
        final int time_dim_length = time_steps_past + time_steps_future + 1;
        final String time_dim_name = matchupFieldsConfig.getTime_dim_name();

        final Dimension timeDimension = writer.addDimension(time_dim_name, time_dim_length);
        dimensions.add(timeDimension);

        return dimensions;
    }

    // package access for testing purpose only tb 2020-12-03
    Map<String, TemplateVariable> getVariables(MatchupFieldsConfiguration configuration) {
        final HashMap<String, TemplateVariable> variablesMap = new HashMap<>();

        variablesMap.put("an_sfc_u10", new TemplateVariable(configuration.get_an_u10_name(), "m s**-1", "10 metre U wind component", null, false));
        variablesMap.put("an_sfc_v10", new TemplateVariable(configuration.get_an_v10_name(), "m s**-1", "10 metre V wind component", null, false));
        variablesMap.put("an_sfc_siconc", new TemplateVariable(configuration.get_an_siconc_name(), "(0 - 1)", "Sea ice area fraction", "sea_ice_area_fraction", false));
        variablesMap.put("an_sfc_sst", new TemplateVariable(configuration.get_an_sst_name(), "K", "Sea surface temperature", null, false));
        variablesMap.put("fc_sfc_metss", new TemplateVariable(configuration.get_fc_metss_name(), "N m**-2", "Mean eastward turbulent surface stress", null, false));
        variablesMap.put("fc_sfc_mntss", new TemplateVariable(configuration.get_fc_mntss_name(), "N m**-2", "Mean northward turbulent surface stress", null, false));
        variablesMap.put("fc_sfc_mslhf", new TemplateVariable(configuration.get_fc_mslhf_name(), "W m**-2", "Mean surface latent heat flux", null, false));
        variablesMap.put("fc_sfc_msnlwrf", new TemplateVariable(configuration.get_fc_msnlwrf_name(), "W m**-2", "Mean surface net long-wave radiation flux", null, false));
        variablesMap.put("fc_sfc_msnswrf", new TemplateVariable(configuration.get_fc_msnswrf_name(), "W m**-2", "Mean surface net short-wave radiation flux", null, false));
        variablesMap.put("fc_sfc_msshf", new TemplateVariable(configuration.get_fc_msshf_name(), "W m**-2", "Mean surface sensible heat flux", null, false));

        return variablesMap;
    }
}
