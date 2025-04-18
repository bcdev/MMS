### Updates from version 1.5.8 to 1.6.0
* Migrated to SNAP 11, several refactorings and library updates
* The VariableCache now uses an LRU cache with a listener to automatically free the oldest cached, no longer
  needed variables and file handles when the maximum cache size is reached.* fix wrong corner point order of 
  era5 interpolation in SatelliteFields
* Instead, the four era5 interpolation vertices are read directly from the variable
* Remove reading of subset from entire era5 variable array and remove to read the entire era5 variable in VariableCache
* Remove creation of rectangles for InterpolationContext and remove field era5Region in InterpolationContext
* era5 post processing .. can now also handle satellite longitude data which not fits the range [-180 to 180].
  In such cases (e.g. Windsat-Coriolis [0 to 360]), longitude data will be converted so that it fits into the
  required range of [-180 to 180] to be able to create correct interpolation of era5 data.
* era5 post processing .. In era5-post-processing-general-info.xml a fill value per
  variable can be defined.
* era5 post processing .. can now load generalized information from an optional
  era5-post-processing-general-info.xml in the config directory.
  A general info file can contain variable definition for satellite-fields. 
* Upgrade Mockito to version 4.11.0 and also include artifact mockito-inline for static mocking
* era5 post processing .. setter and getter of variable names in class SatelliteFieldsConfiguration was
  generalized. E.g. config.set_an_ml_q_name("abcde") was replaced by config.setVarName("an_ml_q", "abcde")  
* era5 post processing .. "length" attribute of tag <satellite-fields><z_dim name='up' length='???' />" is no
  longer optional. 
* era5 post processing .. The translation of variable name to variable name for file access has been made
  switchable. 
* post processing .. added PostProcessingContext to method PostProcessingPlugin.createPostProcessing(...).
  Now plugins can perform extended initializations. E.g. by loading extra files from config directory. 
* post processing .. the config directory has been added to the PostProcessingContext so that plugins can load 
  additional generalized information required for the plugin from this directory.

### Updates from version 1.5.7 to 1.5.8
* added support for SMAP Salinity L2C data  
* added functionality to configure several global attributes before computing MMD files.
* added support for NOAA TAO insitu data 
* added support for NOAA NDBC insitu data
* added support for Windsat Coriolis data
* added conda environment
* extended workflow to run without scheduler on plain console
* Added option to use different name for matchup dimension in post processing

### Updates from version 1.5.6 to 1.5.7
* added support for SMOS L1C daily aggregated products
* added support for SIC-CCI RRDP insitu data
* Corrected reading bug in slstr-s3x-uor reader

### Updates from version 1.5.5 to 1.5.6
* added support for SLSTR subset data processed by UoR
* updated to use SNAP 9.0.2 / S3TBX 9.0.3
* corrected TAI 1993 to UTC conversion for MODIS data
* added support for MODIS MxD035 cloud data
* updated H2 database driver
* added option for IngestionTool to update already existing entries
* changed database to use relative archive paths and added migration tool
* updated SLSTR reader to allow switching geo-coding source between tie-point/pixel
* added database timeout to configuration
* Updated ERA5 post-processing - simplify configuration
* Fixed bug in ERA5 post-processing - time series iteration was incorrect

### Updates from version 1.5.4 to 1.5.5
* updated to use SNAP version 8.0.9 / S3TBX version 8.0.6
* updated to use Apache Commons DBCP2 2.9.0, postgres jdbc 42.3.3, MongoDB jdbc and bson 3.12.10, JUnit 4.13.2, Mockito 4.4.0
* added shutdown hook for resource cleanup on hard shutdowns 
* corrected fill value handling for time-variable in ERA-5 post processing
* corrected ERA-5 raster position calculations
* corrected interpolation error in ERA-5 post processing

### Updates from version 1.5.3 to 1.5.4
* updated to use SNAP version 8.0.5 / S3TBX version 8.0.3
* update Postgres/PostGIS drivers
* corrected bug in Postgres connection where segmented geometries were read incorrectly sometimes
* added support for SIRDS SST insitu data

### Updates from version 1.5.2 to 1.5.3
* updated to use SNAP version 8.0.3
* added random string to SLSTR uncompressed temp directory
* updated workflow handling to new PMonitor version
* updated random seed strategy: keeps on iterating after IOException
* update MxD021km reader - split vector based attributes to channel layer
* added support for AVHRR FRAC MetOp-C L1b
* added post-processing to add ERA-5 NWP data
* fixed issues with SNAP readers and non-quadratic extraction window 

### Updates from version 1.5.1 to 1.5.2
* updated to use SNAP version 8.0-SNAPSHOT
* updated to use NetCDF version 5.3.1
* added facility to store scaled variables to MMD
* added support for MxD021KM MODIS level 1 data
* added support for AVHRR FRAC MetopB L1B
* implemented SLURM support
* corrected too-far-apart matches issue when using SOBOL based sampling

### Updates from version 1.5.0 to 1.5.1
* update to support HIRS FCDR v1.00
* updated SNAP dependency to latest version (7.0.3, S3TBX 7.0.2)

### Updates from version 1.4.9 to 1.5.0
* corrected bug in SLSTR reader - read over product borders
* added data caching for SLSTR reader

### Updates from version 1.4.8 to 1.4.9
* added support for compressed SLSTR L1B data
* corrected SeedPointMatchupStrategy coordinate rounding
* corrected SLSTR L1B time coding calculations
* updated AVHRR FCDR naming regular expression
* corrected post-processing for AVHRR FCDR correlation coefficients

### Updates from version 1.4.7 to 1.4.8
* implemented support for segmented geo-location data for AVHRR GAC
* added support for AVHRR GAC v2.10.2 data

### Updates from version 1.4.6 to 1.4.7
* added inverse cosine scaling for Sobol random sequences
* added post-processing for AVHRR FCDR correlation coefficients
* added support for SLSTR L1B data
* added support for AVHRR_GAC v1,5, file version 2.0 format (v1.5.2)
* changed Mongodb socket timeout to 4 minutes

### Updates from version 1.4.5 to 1.4.6
* added option to switch Sobol sequences between equal-area and equal-angle sampling.

### Updates from version 1.4.4 to 1.4.5
* corrected AVHRR FCDR reader regexp for filenames

### Updates from version 1.4.3 to 1.4.4
* updated AVHRR FCDR reader to match format changes

### Updates from version 1.4.2 to 1.4.3
* added support for compressed AVHRR FRAC data products
* corrected NetCDF lib fill value initialisation during write operations

### Updates from version 1.4.1 to 1.4.2
* corrected bug in distance-to-land: NPE when two or more instances are allocated
* added support for FIDUCEO AVHRR FCDR products
* added support for FIDUCEO HIRS FCDR products
* added support for AVHRR FRAC data products
* updated scripts to new JASMIN file system
* added support for GRUAN reference files
* added optional paging and offset to database drivers
* updated to SNAP 6.0.8 
* updated 3dr party libraries
* added DB maintenance tool

### Updates from version 1.4.0 to 1.4.1
* migrated to asynchronous PMonitor
* AIRS reader implemented
* AIRS post processing to add channel data implemented
* Bugfix BowTiePixellocator

### Updates from version 1.3.9 to 1.4.0
* Improve NWP Post Processing 

### Updates from version 1.3.8 to 1.3.9
* Improved performance of IASI geo-coding
* Speed up in polar-orbiting matchup strategy
* Corrected handling of multiple intersecting areas in one orbit file

### Updates from version 1.3.7 to 1.3.8
* Added optional "insitu-sensor" parameter to SSTInsituTimeSeries post-processing configuration
* Added post-processing that copies the AMSR2 Scan_Data_Quality variable to the MMD
* Renamed PostProcessing Plugin AddAmsreSolarAngles, expanded to be applicable also for AMSR-2

### Updates from version 1.3.6 to 1.3.7
* Update to use SNAP version 6.0.0
* Corrected bug in PostGRES database driver
* Maintenance of core modules

### Updates from version 1.3.5 to 1.3.6
* Implemented support for AMSR 2 data
* Added functionality to extract all overflights over a single location
* Added global temp-file handling with automated cleanup
* Improved geocoding for MxD06 sensor data, improved accuracy

### Updates from version 1.3.4 to 1.3.5
* Improved check if product is already ingested to speed up database ingestion
* Improved geocoding for MxD06 data, bow tie case, to handle with data gaps

### Updates from version 1.3.3 to 1.3.4
* Bugfix. Corrected day of year (DOY) handling at PathContext.

### Updates from version 1.3.2 to 1.3.3
* Bugfix. Fix read for AcquisitionInfo of CALIOP_xxx_Reader. Caliop reader extended to be able to create product bounding geometry for very small products.

### Updates from version 1.3.1 to 1.3.2
* Urgent workaround to resolve NetCDF v4.6.10 bug

### Updates from version 1.3.0 to 1.3.1
* Caliop L2 CLay Post Processing to add CLay data to an MMD file which already contain Caliop VFM Data.

### Updates from version 1.2.9 to 1.3.0
* Added condition plugin to ensure unique samples per MMD file
* Implemented support for AVHRR GAC L1C data in version 1.5

### Updates from version 1.2.8 to 1.2.9
* Added satellite sensor reader for CALIOP L2 Clay products
* Implemented support for MxD06 Modis cloud products
* Fixed bug in NWP post processing plugin configuration parsing

### Updates from version 1.2.7 to 1.2.8
* Implemented support for AVHRR GAC L1C data in version 1.3 and 1.4
* Updated SST-In-situ reader to support v04.0 data

### Updates from version 1.2.6 to 1.2.7
* Implemented support for OceanRain insitu SST data

### Updates from version 1.2.5 to 1.2.6
* MMD - Writer ensures CF conform usage of variable attribute "units"
* Initialize ReaderCache from system or writer configuration 
* Corrected SeedPointStrategy to use numPointsPerDay
* Post Processing for CALIOP Feature_Classification_Flags

### Updates from version 1.2.4 to 1.2.5
* Optimized resource usage for distance-to-land calculations
* Updated NWP post processing to accept scaled integer geolocation data
* Added post processing that calculates the distance to land
* Added support for matchups with multiple secondary sensors
* Added satellite sensor reader for CALIOP L2 VFM products
* Border distance condition is ready to support multiple secondary sensors
* Time Delta condition is ready to support multiple secondary sensors
* Overlap remove condition is ready to support multiple secondary sensors
* Window value screening is ready to support multiple secondary sensors

### Updates from version 1.2.3 to 1.2.4
* Fixed bug in IASI reader that caused crashes when reading files significantly larger than 2 GB
* Updated third party libraries: NetCDF, MongoDB, H2, MySQL and SNAP

### Updates from version 1.2.2 to 1.2.3
* Added "Sun_Glint_Angle" variable to AMSR-E reader
* Extended IASI reader to support v4 and v5 MDR data
* Implemented post processing to add IASI spectrum to MMD
* Added IASI reader for EUMETSAT format
* Fixed bug in AngularScreening that removed too many pixels in some cases.

### Updates from version 1.2.1 to 1.2.2
* Extended Archive class to understand DAY_OF_YEAR elements
* Added post processing plugin to convert elevation to zenith angles
* Improved PostProcessingTool - now creates target directory if not existing

### Updates from version 1.2.0 to 1.2.1
* Updated BorderDistance condition to allow distinguishing between reference and secondary sensor
* Added RFI glint variables to AMSR-E reader
* Added post processing plugin to add ERA-interim NWP data to matchup datasets

### Updates from version 1.1.3 to 1.2.0
* Implemented post processing engine
* Added spherical distance post-processing plugin
* Added AMSRE solar angles post-processing plugin
* Added SST in-situ time series extraction post-processing plugin
* Added WindowValue screening plugin

### Updates from version 1.1.2 to 1.1.3
* Added reader cache size parameter to MMD writer configuration
* Updated MMDWriter to store CF conforming default fill value attributes

### Updates from version 1.1.1 to 1.1.2
* Fixed bug in AVHRR reader that causes crashes when extracting 1x1 pixel windows

### Updates from version 1.1.0 to 1.1.1
* Implemented full support for PostGIS databases

### Updates from version 1.0.5 to 1.1.0
* Added condition plugin for overlap removal
* Added support for SST-CCI insitu data

### Updates from version 1.0.4 to 1.0.5
* Migrated system configuration from properties to XML format
* Implemented configurable archiving rules

### Updates from version 1.0.3 to 1.0.4
* Added MMD Writer configuration file
* Added MMD variable renaming engine

### Updates from version 1.0.2 to 1.0.3
* Added reader for SSM/T-2 data
* Added full support for Apache H2 database
* Renamed parameter for AngularScreening plugin
* Added MMD writer configuration
* Fixed bug in HIRS reader plugin that prevented HIRS NOAA 18 data to be handled

### Updates from version 1.0.1 to 1.0.2
* Added HIRS "lza" angular screening
* Added HIRS L1C reader
* Updated regular expression for AMSU-B/MHS reader
* Added reader for ATSR1, ATSR2 and AATSR L1B data in ENVISAT format
* Added optional calculation of matchup center distance variable
* Added PixelValue screening plugin
* Corrected fill value handling to follow CF conventions
* Added reader form AMSR-E L2A data in HDF format
* Added support for multiple processing version of sensor data

### Updates from version 1.0.0 to 1.0.1
* Implemented cloud screening algorithm for AMSU-B, MHS and SSMIS according to University of Hamburg
* Fixed issue where input files were not closed correctly
* Fixed performance bottleneck when adding secondary observation pixels

