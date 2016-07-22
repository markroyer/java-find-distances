# java-find-distances

A simple Java program that reads a CSV file containing vehicles and locations 
and uses the Google Maps API to find the distance to a provided location.

The program can be run in the following manner.

```
Usage: java edu.umaine.cs.FindDistance input_file.csv 'City, State Abbreviation'
 
For example,
 
java edu.umaine.cs.distance.FindDistance exampleInput.csv 'Orono, ME'
```

The input file must be a comma separated file with the following elements:

```
id,vehicle description 1,vehicle description 2,license plate,year,city,state
```

An example input file named `exampleInput.csv` has been provided.

When the program is run, it will create a JSON cache file called 
`cachedLocations.json` to store distances.  The program uses this cache file on 
subsequent runs.

The name of the result file is the input file with the word Out appended to it. 
The contents of the file is as follows:

```
id,Make,Model,Plate,Year,City,State,Distance (m),Time (s),Origin,Destination,Status
```

## Dependencies

This project makes use of the GSON library. A copy has been included in the 
lib directory.

## Building

The project was written using the Eclipse IDE, so the easiest way to build the 
project is to import the existing project into Eclipse.

## License

The project is licensed under the terms of the
[GPL3](https://www.gnu.org/licenses/gpl-3.0.en.html) license.

<!--
LocalWords:  java CSV API csv exampleInput Orono JSON cachedLocations
LocalWords:  json GSON lib IDE GPL
-->