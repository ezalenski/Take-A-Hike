# Take-A-Hike
Java program that takes ArcGIS ASCII Grid data from [NOAA Grid Extract](http://maps.ngdc.noaa.gov/viewers/wcs-client/) and finds the best path with the least net change of elevation over the path. 

Uses 2 algorithms, greedy and Dijkstra's and draws the greedy path from the lowest elevation(in red), best greedy path(in green), and then best path found with Dijkstra's(in blue). Example output in [here](result.png).
