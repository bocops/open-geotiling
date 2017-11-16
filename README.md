# open-geotiling

A set of additional functions for Open Location Code, based on the idea that every plus code identifies
a rectangular surface area (a "tile") of varying size, making it useful not only for _locations_ but also _areas_.

Using this wrapper class allows to
* determine whether two tiles are the same, or if the bigger contains the smaller
* determine whether two tiles are adjacent, even if of different size
* determine all neighboring tiles of a given one
* calculate a distance in tiles
* get an approximate direction from one tile to another

Open Location Code is a technology developed by Google and licensed under the Apache License 2.0.

Links
-----
 * [Open Location Code](https://github.com/google/open-location-code)

