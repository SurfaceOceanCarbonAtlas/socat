#!/bin/csh 
#

ncatted -O -h -a title,global,o,c,'SOCAT gridded v2020 Monthly quarter-degree Coastal gridded dataset' SOCAT_qrtrdeg_gridded_coast_monthly.nc
ncatted -O -h -a summary,global,o,c,'Surface Ocean Carbon Atlas (SOCAT) Gridded (binned) SOCAT observations, masked for coastal data only, \nwith a spatial grid of quarter-degree and monthly in time. The gridded fields are computed using only \nSOCAT cruises with QC flags of A through D and SOCAT data points flagged with WOCE flag values of 2. \nThe grid is monthly in time and quarter-degree in longitude and latitude. A coastal mask is applied to \nmatch the 400-Km coastal region of the SOCAT database.' SOCAT_qrtrdeg_gridded_coast_monthly.nc
ncatted -O -h -a references,global,o,c,'http://www.socat.info/' SOCAT_qrtrdeg_gridded_coast_monthly.nc
ncatted -O -h -a SOCAT_Notes,global,o,c,'SOCAT gridded v2020 05-June-2020' SOCAT_qrtrdeg_gridded_coast_monthly.nc
ncatted -O -h -a caution,global,o,c,'NO INTERPOLATION WAS PERFORMED. SIGNIFICANT BIASES ARE PRESENT IN THESE GRIDDED RESULTS DUE TO THE \nARBITRARY AND SPARSE LOCATIONS OF DATA VALUES IN BOTH SPACE AND TIME.' SOCAT_qrtrdeg_gridded_coast_monthly.nc

ncatted -O -h -a summary,coast_count_ncruise,o,c,'Number of cruises containing observations in the grid cell.' SOCAT_qrtrdeg_gridded_coast_monthly.nc

ncatted -O -h -a summary,coast_fco2_count_nobs,o,c,'Total number of observations in the grid cell.' SOCAT_qrtrdeg_gridded_coast_monthly.nc
ncatted -O -h -a summary,coast_fco2_ave_unwtd,o,c,'Arithmetic mean of all fco2 recomputed values found in the grid cell.' SOCAT_qrtrdeg_gridded_coast_monthly.nc
ncatted -O -h -a summary,coast_fco2_ave_weighted,o,c,'Mean of fco2 recomputed computed by calculating the arithmetic mean value for each cruise passing through the cell and then averaging these cruises.' SOCAT_qrtrdeg_gridded_coast_monthly.nc
ncatted -O -h -a summary,coast_fco2_max_unwtd,o,c,'Maximum value of fco2 recomputed observed in the grid cell.' SOCAT_qrtrdeg_gridded_coast_monthly.nc
ncatted -O -h -a summary,coast_fco2_min_unwtd,o,c,'Minimum value of fco2 recomputed observed in the grid cell.' SOCAT_qrtrdeg_gridded_coast_monthly.nc
ncatted -O -h -a summary,coast_fco2_std_unwtd,o,c,'The standard deviation of fco2 recomputed computed from the unweighted mean.' SOCAT_qrtrdeg_gridded_coast_monthly.nc
ncatted -O -h -a summary,coast_fco2_std_weighted,o,c,'A weighted standard deviation of fco2 recomputed computed to account for the differing variance estimates for each cruise passing through the cell. The statistical technique is described at http://wapedia.mobi/en/Weighted_mean#7.' SOCAT_qrtrdeg_gridded_coast_monthly.nc

ncatted -O -h -a summary,coast_sst_count_nobs,o,c,'Total number of observations in the grid cell.' SOCAT_qrtrdeg_gridded_coast_monthly.nc
ncatted -O -h -a summary,coast_sst_ave_unwtd,o,c,'Arithmetic mean of all sst values found in the grid cell.' SOCAT_qrtrdeg_gridded_coast_monthly.nc
ncatted -O -h -a summary,coast_sst_ave_weighted,o,c,'Mean of sst computed by calculating the arithmetic mean value for each cruise passing through the cell and then averaging these cruises.' SOCAT_qrtrdeg_gridded_coast_monthly.nc
ncatted -O -h -a summary,coast_sst_max_unwtd,o,c,'Maximum value of sst observed in the grid cell.' SOCAT_qrtrdeg_gridded_coast_monthly.nc
ncatted -O -h -a summary,coast_sst_min_unwtd,o,c,'Minimum value of sst observed in the grid cell.' SOCAT_qrtrdeg_gridded_coast_monthly.nc
ncatted -O -h -a summary,coast_sst_std_unwtd,o,c,'The standard deviation of sst computed from the unweighted mean.' SOCAT_qrtrdeg_gridded_coast_monthly.nc
ncatted -O -h -a summary,coast_sst_std_weighted,o,c,'A weighted standard deviation of sst computed to account for the differing variance estimates for each cruise passing through the cell. The statistical technique is described at http://wapedia.mobi/en/Weighted_mean#7.' SOCAT_qrtrdeg_gridded_coast_monthly.nc

ncatted -O -h -a summary,coast_salinity_count_nobs,o,c,'Total number of observations in the grid cell.' SOCAT_qrtrdeg_gridded_coast_monthly.nc
ncatted -O -h -a summary,coast_salinity_ave_unwtd,o,c,'Arithmetic mean of all salinity values found in the grid cell.' SOCAT_qrtrdeg_gridded_coast_monthly.nc
ncatted -O -h -a summary,coast_salinity_ave_weighted,o,c,'Mean of salinity computed by calculating the arithmetic mean value for each cruise passing through the cell and then averaging these cruises.' SOCAT_qrtrdeg_gridded_coast_monthly.nc
ncatted -O -h -a summary,coast_salinity_max_unwtd,o,c,'Maximum value of salinity observed in the grid cell.' SOCAT_qrtrdeg_gridded_coast_monthly.nc
ncatted -O -h -a summary,coast_salinity_min_unwtd,o,c,'Minimum value of salinity observed in the grid cell.' SOCAT_qrtrdeg_gridded_coast_monthly.nc
ncatted -O -h -a summary,coast_salinity_std_unwtd,o,c,'The standard deviation of salinity computed from the unweighted mean.' SOCAT_qrtrdeg_gridded_coast_monthly.nc
ncatted -O -h -a summary,coast_salinity_std_weighted,o,c,'A weighted standard deviation of salinity computed to account for the differing variance estimates for each cruise passing through the cell. The statistical technique is described at http://wapedia.mobi/en/Weighted_mean#7.' SOCAT_qrtrdeg_gridded_coast_monthly.nc

ncatted -O -h -a summary,coast_lat_offset_unwtd,o,c,'The arithmetic average of latitude offsets from the grid cell center for all observations in the grid cell. The value of this offset can vary from -0.5 to 0.5. A value of zero indicates that the computed fco2 mean values are representative of the grid cell center position.' SOCAT_qrtrdeg_gridded_coast_monthly.nc
ncatted -O -h -a summary,coast_lon_offset_unwtd,o,c,'The arithmetic average of longitude offsets from the grid cell center for all observations in the grid cell. The value of this offset can vary from -0.5 to 0.5. A value of zero indicates that the computed fco2 mean values are representative of the grid cell center position.' SOCAT_qrtrdeg_gridded_coast_monthly.nc


ncdump -h  SOCAT_qrtrdeg_gridded_coast_monthly.nc
