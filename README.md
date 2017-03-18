# farthest-point
Numeric solver for locating the point in the continental mainland US that is farthest from a provided set of points.
The definition of US mainland is taken from
[United States Census Bureau](https://www.census.gov/geo/maps-data/data/cbf/cbf_nation.html)
, removing everything except a single polygon representing the mainland.
The list of points must be supplied as a CSV file containing at least the latitude and longitude for each point. For example,
the list of US public libraries is available at [AggData](https://www.aggdata.com/public_libraries). Once you have the file,
you can invoke the search with

```
./gradlew run -Dexec.args="-f public_libraries.csv -d {1},{3},{4},{5},{6} --lat 9 --lon 10"
```

where f is the location of the file, d is a template to describe a row (passed to MessageFormat with the columns in the
csv file as arguments), lat and lon are the zero-based indexes of the columns containing the latitude and longitude.

## Algorithm
My original intention was to solve this precisely using spherical Voronoi diagrams. Some experimentation shows that there are
too many special cases
where the farthest point lies either at a vertex of the boundary polygon or even on an edge of the boundary polygon, midway
between two of the data points. So a simple numerical algorithm is more effective since it can handle any case and return
an answer to any required degree of precision.

The current algorithm divides the search space into 'squares' and then either eliminates each sqare as being too close to a
point or divides the square into four and iterates. With no particular optimization, this solves the public library problem
(with 26161 data points) in under a minute.

There are a few rough edges: the worst-case mid-square distance is correct for Euclidean distance but not quite for Haversine.
This doesn't affect the accuracy of the answer but could lead to finding an incorrect answer in highly pathological cases.
Similarly, we don't perform an accurate square-polygon intersection test which could result in us dismissing a square that
actually contains the correct answer. Again this would require a carefully constructed data set to force a problem.
