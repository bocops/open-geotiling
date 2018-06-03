# Contributing
This repository contains the original Java implementation of the "Open Geotiling" library,
which is based on [Google's Open Location Codes](https://github.com/google/open-location-code). Besides the original Java implementation,
ports to other languages might exist in separate directories.
Contributions to this project are welcome - depending on the type of your contribution, there are different ways to do so:

## Help create this library and define its API
This library is still in its early stages.
* If you found a bug in the implementation maintained here, you can either open an issue about it,
or fix it yourself and send a pull request.
    * Before sending a pull request containing code, please make sure that it is complete, passes all existing tests,
    and that tests for the bug you fixed have been added.
* Similarly, if there's missing functionality that you think might be within the scope of this project,
feel free to open a new issue to discuss your ideas. If, after that discussion, the idea turns out to be something we'd like to add,
pull requests are welcome here as well.

At some point, existing functionality will be finalized as a v1.0 API. A link to this API definition will be added here.

## Port the library to other languages
If you consider this library to be useful to you, but you need the functionality in a different language, feel free to port it.
Currently, the best option to do so would be to:
1. Fork the repository to be notified of upstream changes
2. Create your port in a directory named after the programming language (e.g. `python/`)
3. Once the port is complete (especially if an official API has already been defined),
    1. create a file "PORTS.md" in that directory (e.g. `python/PORTS.md`) with a link to your repository,
    or update the file if it already exists
    2. send a pull request for this file, but not the full implementation!

We're currently not generally accepting pull requests for implementations themselves.
Contact us if you feel that this should be handled differently.
