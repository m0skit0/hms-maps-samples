Google Maps Android API Demos
===================================

These are demos for the [Google Maps Android API v2](https://developers.google.com/maps/documentation/android-api/) in Kotlin.
They demonstrate most of the features available in the API.

This app was written for a minSdk of 15 and the v4 support library, but it can be easily adapted to
use native functionality instead.
(For example replacing ``SupportMapFragment`` with ``MapFragment``.)

Getting Started
---------------

This sample use the Gradle build system.

First download the samples by cloning this repository or downloading an archived
snapshot. (See the options at the top of the page.)

In Android Studio, use "Open an existing Android Studio project". Next select the kotlin/ directory that you downloaded
from this repository. If prompted for a gradle configuration accept the default settings.

Alternatively use the "gradlew build" command to build the project directly.

Add your API key to the file `debug/values/google_maps_api.xml`.
It's pulled from there into your app's `AndroidManifest.xml` file.
See the [quick guide to getting an API key](https://developers.google.com/maps/documentation/android-api/signup).

Support
-------

TODO StackOverflow and Huawei Developers Documentation?

License
-------

TODO Decide license (Apache?)