# HiddenLayer Jenkins Model Scanner Plugin

This plugin submits models to the HiddenLayer Model Scanner service for scanning. The plugin can be configured to fail the Jenkins pipeline in the case that the model is flagged as malicious.

## Features

- Submit models to the HiddenLayer Model Scanner service for scanning
- Configuration option to fail the pipeline if the file type is unknown
- Configuration option to fail the pipeline if the file is flagged as malicious

## Configuration

The following fields must be configured in the Jenkins pipeline:

- `ML Model Name` - the name of the model to be scanned
- `HiddenLayer Client ID` - the client ID for the HiddenLayer Model Scanner service
- `HiddenLayer Client Secret` - the client secret for the HiddenLayer Model Scanner service
- `Folder to Scan` - the folder containing the model to be scanned
- `Fail Build on Unsupported Model` - whether to fail the pipeline if the file type is unknown
- `Fail Build if Scan Severity Equal to or Higher` - the severity level at which to fail the pipeline.  Options are `Low`, `Medium`, `High`, and `Critical`.
