# agsservicetesting
Testing AGS using JMeter.  Also includes scripts for setting up and reviewing results.

## Apache JMeter

[Apache JMeter](http://jmeter.apache.org/) is a application for testing Web Applications.

You can download JMeter then untar/unzip it.  

## Run from GUI

Start Apache JMeter.  Run bat file or sh script in the bin folder. 

Open [LoadTest1.jmx](./jmeter/plans/LoadTest1.jmx).  This XML file was created from using the GUI.  

At the top of the plan (XML) there is a section of User Defined Properties.
- bbox_samples_folder
- service_folder
- service_name1
- log_folder
- server
- port 

You'll need to set these for your configuration.  

In the Thread Group you set the number of threads (users) and the Loop Count. For example setting users to 5 and loop count to 100. Each user will execute each request 100 times.  

The Simple Controler contains a sequence of example commands.  
- Request to Map Service to get general info about the service
- Request to Map Service to get legend info
- Four requests to MapServer service
- Four reuquest to FeatureServer service

The Map and Feature service calls each read a envelope from bbox_sample files in the bbox_samples_folder. The [createSampleBbox.py](./scripts/createSampleBbox.py) Python script was used to create the bbox_sample files. Using files like this allows you to get a random set of inputs and repeat the tests with this same input as needed.

## Reviewing the Results
The Summary Report provides overall stats for a particular test run. You can reset the stats using menu Run -> Clear All.

There are other samplers that allow you to view results, such as View Results in Table. These are useful for short runs when troubleshooting; however, they should not be enabled for performance tests.

The Contant Throughput Timer can be used to thottle the inputs. For example when set to 60/min; JMeter will try to to throttle the request to achieve this throughput.

## Running at Command line
You'll need to download JMeter to the server. On Linux you can do this at command line with a curl command.

<pre>
$ curl -O http://apache.org/dist/jmeter/binaries/apache-jmeter-3.1.tgz
</pre>

You should check the md5sum
<pre>
md5sum apache-jmeter-3.1.tgz 
f439864f8f14e38228fee5fab8d912b0  apache-jmeter-3.1.tgz 
</pre>

You'll need to edit the LoadTest1.jmx by hand using a text editor.  

Now you can run the plan.  Assuming you've unzipped apache-jmeter in your home directory.

<pre>
$ ~/apache-jmeter-3.1/bin/jmeter -n -t jmeter/plans/LoadTest1.jmx 
</pre>

You'll see some output on you're screen as it's running.

The reports are written to the logs folder.

The Summary report includes a line for each request.  The [StatsSummary.py](./scripts/StatsSummary.py) Python script can be used to read the report and output average calls per second for each request type.

