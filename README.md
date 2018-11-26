# cspline
Experimental Java program to generate [IGES](https://en.wikipedia.org/wiki/IGES) files containing B-spline surfaces.

The input to this program is one or more JSON files, which contain B-spline surfaces.
The input format is described in the [splinefit](https://github.com/ooreilly/splinefit) repository.

The output is an IGES file.
IGES is a standard CAD file format, which can be read by [Cubit](https://cubit.sandia.gov/) and other software.
IGES is an old format, but it has the advantages that it is ASCII, it includes B-spline surfaces as one of its native geometry types,
and there is a publicly-available 700-page [specification](https://filemonger.com/specs/igs/devdept.com/version6.pdf).

This program was created as part of a pilot project to mesh a small subset of the [Community Fault Model](https://scec.usc.edu/scecpedia/CFM).

# Installation
Before starting, make sure you have a Java development kit installed on your system.
The program requires Java 8 or higher.
The recommended versions of Java are Oracle Java or OpenJDK.

## Clone the repository
To make a local copy of the repository on your own computer, change to the directory where you want the repository to be installed and then:
```bash
$ git clone https://github.com/mbarall/cspline
```

## Compile the program
```bash
$ cd cspline
$ ./gradlew fatJar
```
The compiled Java code is in the file `cspline-all.jar` which is located in the directory `cspline/build/libs`.

Note: Gradle is used to manage the build process.
If you don't already have Gradle installed on your system, then the `gradlew` command will automatically download and install Gradle,
which can take a considerable amount of time.
Subsequent builds will be much faster.

# Running the program
To run the program on all the CFM pilot project files, use this command:
```bash
$ java -cp cspline/build/libs/cspline-all.jar scratch.spline.FittedSpline cfm_pilot_to_iges src_dir dest_dir
```
In the above command, replace `src_dir` with the directory where the CFM pilot project JSON files are located,
for example, `CFM5_pilot_bspline_beta_1.1_2018`.
Replace `dest_dir` which the directory where you want to put the resulting IGES files.
The program will create the `dest_dir` directory if it does not already exist.
It generates one IGES file for each of the faults in the CFM pilot project.

Inside the `FittedSpline` class there are other functions that can be used to create IGES files from the
JSON files of your choice.

