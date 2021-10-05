# sss
Super Simple Sonic - a super simple subsonic compatiblish server

The current implementation:
* Is probably as secure as Spark 2.9.3 is, but I'm no security expert, so no guarantees.
* Assumes the music directory to be /var/lib/mpd/music
* Assumes directory format to be always Artist/Album/Files
* Needs ffmpeg and ffprobe binaries to be installed
* Probably only works on *nixes
* Creates a db.sqlite in the current working directory
* Only supports json format, as supported by e.g. the [Ultrasonic](https://www.f-droid.org/en/packages/org.moire.ultrasonic/) client.
* Is sloppy in some other ways
* As of Sep 2021 has a patched version of minisub included as a git submodule, acting as a web interface

But:
* Works for me (TM) ;-) even though I will probably develop it into a little more mature product.
* Can be run using `mvn clean package` and then `java -jar target/sss-0.1-SNAPSHOT-jar-with-dependencies.jar`
