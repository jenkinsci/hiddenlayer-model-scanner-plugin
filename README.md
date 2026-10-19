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

## Agent requirements

`hlScanModel` runs the HiddenLayer client on the Jenkins node that owns the workspace, not on the controller. That node needs outbound access to `api.hiddenlayer.ai`. Client ID and secret are sent to the agent for the duration of the scan. Optional SDK debug logging (`HIDDENLAYER_LOG`) is read from the agent's environment.

If the controller has a proxy configured in Jenkins, that proxy is used for HiddenLayer API calls. If it does not, the plugin does not set a proxy on the agent.
