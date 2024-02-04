# Overview

This projects contains a jenkins shared library which is used to define CICD pipelines for **encoding-app** and **online-boutique**.  

# Pipelines

* Pipelines and helper functions are defined in `vars` directory.  
* Pipelines used for **encoding-app**:  
| Pipeline        | Description |
|-----------------|
| encoderPreMerge   | automatically triggered when a PR is open on [encoding-app backend service repository](https://github.com/anea-11/x265) |
| encoderPostMerge  | automatically triggered when there's a merge to master on [encoding-app backend service repository](https://github.com/anea-11/x265) |
| deployEncodingApp | manually triggered to deploy **encoding-app** |