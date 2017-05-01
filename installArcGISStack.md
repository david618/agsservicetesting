
# Installing ArcGIS Stack 

To use GeoEvent with BDS you'll need to install ArcGIS Portal, Web Adaptor, Server, GeoEvent, Relational Datastore, and Spatiotemportal Datastore.

There are detailed Installation Guies for each
- [ArcGIS Portal](http://server.arcgis.com/en/portal/latest/install/windows/welcome-to-the-portal-for-arcgis-installation-guide.htm)
- [ArcGIS Server](http://server.arcgis.com/en/server/latest/install/windows/welcome-to-the-arcgis-for-server-install-guide.htm)
- [Web Adaptor](http://server.arcgis.com/en/web-adaptor/latest/install/iis/welcome-arcgis-web-adaptor-install-guide.htm)
- [Data Store](http://server.arcgis.com/en/data-store/latest/install/windows/welcome-to-arcgis-data-store-installation-guide.htm)
- [GeoEvent](http://server.arcgis.com/en/geoevent/latest/install/windows/installation-steps.htm)

Below are my notes for installing on Linux; that I use a a guide for installation.

## Install system requirements as root
<pre>
yum -y install bash-completion
yum -y groupinstall x11
yum -y install dos2unix
yum -y install freetype
yum -y install libXfont
yum -y install mesa-libGL
</pre>

## These are only required on web server
<pre>
yum -y install httpd
yum -y install mod_ssl
yum install java-1.8.0-openjdk
</pre>

## Create arcgis user
<pre>
useradd arcgis
</pre>

## Configure limits
<pre>
vi /etc/security/limits.conf
</pre>
Append:
<pre>
arcgis soft nofile 65535
arcgis hard nofile 65535
arcgis soft nproc 25059
arcgis hard nproc 25059
</pre>

## Configure sysctl
Required for BDS Spatiotemportal Store
<pre>
vi /etc/sysctl.conf
</pre>
Append:
<pre>
vm.max_map_count = 262144
vm.swappiness = 1
</pre>

CentOS 7.x 
If tuned is installed; it is by default.
<pre>
vi /usr/lib/tuned/virtual-guest/tuned.conf
</pre>
Set swappiness to 1

Reload configuration
<pre>
sysctl -p
</pre>

## Copy Installers and License Files to server
Copy to /home/arcgis
Set arcgis as owner

## Start Web Server
<pre>
systemctl start httpd
systemctl enable httpd
</pre>

## Install Tomcat 
Since this is for ArcGIS I install in arcgis home.

Tomcat updates frequently so the link below may need to be updated.  Check [Tomcat Site](http://www.apache.org/dist/tomcat/tomcat-8/) and use the latest release.

<pre>
su - arcgis
curl -O http://www.apache.org/dist/tomcat/tomcat-8/v8.0.43/bin/apache-tomcat-8.0.43.tar.gz

md5sum apache-tomcat-8.0.43.tar.gz
9ae13c64db525924440429f6fcd95f84  apache-tomcat-8.0.43.tar.gz

tar xvzf apache-tomcat-8.0.43.tar.gz

ln -s apache-tomcat-8.0.43 tomcat8

exit
</pre>

As root again
<pre>
vi /etc/systemd/system/tomcat8.service
Description=Tomcat8
After=network.target

[Service]
Type=forking
User=arcgis
Group=arcgis

ExecStart=/home/arcgis/tomcat8/bin/startup.sh
ExecStop=/home/arcgis/tomcat8/bin/shutdown.sh

[Install]
WantedBy=multi-user.target
</pre>

Start and Enable Tomcat

<pre>
systemctl start tomcat8
systemctl enable tomcat8
</pre>

## Install Web Adaptor

<pre>
su - arcgis
tar xvzf Web_Adaptor_Java_Linux_105_154055.tar.gz
cd WebAdaptor/
./Setup -m silent -l yes
cd ../webadaptor10.5/java/
cp arcgis.war /home/arcgis/tomcat8/webapps/
cp arcgis.war /home/arcgis/tomcat8/webapps/portal.war

exit
</pre>

## Configure Web Server 
These proxy settings pass request from Web Server to Tomcat/GeoEvent

<pre>
vi /etc/httpd/conf.d/proxy.conf

ProxyRequests off

ProxyPass /portal ajp://localhost:8009/portal
ProxyPassReverse /portal ajp://localhost:8009/portal

ProxyPass /arcgis ajp://localhost:8009/arcgis
ProxyPassReverse /arcgis ajp://localhost:8009/arcgis

SSLProxyEngine on
SSLProxyVerify none
SSLProxyCheckPeerCN off
SSLProxyCheckPeerName off
SSLProxyCheckPeerExpire off
ProxyPass /geoevent https://localhost:6143/geoevent
ProxyPassReverse /geoevent https://localhost:6143/geoevent
</pre>

This SELinux config is necessary to route geoevent 
<pre>
setsebool -P httpd_can_network_connect true
</pre>

Restart web server
<pre>
systemctl restart httpd
</pre>

## Install Portal

Portal and ArcGIS Server need the domain name to be resolvable; I added one to /etc/hosts.

Find the IP for the server.
<pre>
ip -4 -o addr 
1: lo    inet 127.0.0.1/8 scope host lo\       valid_lft forever preferred_lft forever
2: eth0    inet 172.16.1.5/24 brd 172.16.1.255 scope global eth0\       valid_lft forever preferred_lft forever
</pre>

Add entry to hosts 
<pre>
vi /etc/hosts

172.16.1.5 dj32ags.westus.cloudapp.azure.com
</pre>

** NOTE: Add this same entry into test server. **

<pre>
su - arcgis

tar xvzf Portal_for_ArcGIS_Linux_105_154053.tar.gz
cd PortalForArcGIS/
./Setup -m silent -l yes -a /home/arcgis/portal_25_50.ecp
</pre>

You'll see instructions to access https://localhost:7443/arcgis/home; instead access using the dns name (e.g.  https://dj32ags.westus.cloudapp.azure.com:7443) or you can use a ssh tunnel (e.g. ssh -L 7443:localhost:7443) and temporarily set your system's hosts  to include the name dj32ags.westus.cloudapp.azure.com as a valid name for localhost.  

In your browser you'll get a warning about the self-signed certificate.  Export the certificate to a file; you'll need it later.

Create New Portal. 

## Install Server

<pre>
tar xvzf ArcGIS_Server_Linux_105_154052.tar.gz
cd ArcGISServer/
./Setup -m silent -l yes -a /home/arcgis/Server_Ent_Adv.ecp
</pre>

You'll see instructions to access https://<server name>:6080/arcgis/manager; as you did for Portal use external DNS name or tunnel. 

Create New AGS Site

## Add Certificates to ArcGIS Server
In browser go to https://dj32ags.westus.cloudapp.azure.com
In your browser you'll get a warning about the certificate.  Export the certificate to a file.

On the AGS that will also have GeoEvent installed access the /arcgis/admin page.

https://dj32ags.westus.cloudapp.azure.com:6443/arcgis/admin

Login 

Under machines -> dj32ags.westus.cloudapp.azure.com -> sslcertificates

Click importRootOrIntermediate certificates

Import the certificate from https://dj32ags.westus.cloudapp.azure.com  (portal)
And from https://dj32ags.westus.cloudapp.azure.com:7443 (portal_7443)

## Configure Web Adaptors

<pre>
~/webadaptor10.5/java/tools/configurewebadaptor.sh -m portal -w https://dj32ags.westus.cloudapp.azure.com/portal/webadaptor -g https://dj32ags.westus.cloudapp.azure.com:7443 -u portaladmin -p &lt;PASSWORD&gt;

~/webadaptor10.5/java/tools/configurewebadaptor.sh -m server -w https://dj32ags.westus.cloudapp.azure.com/arcgis/webadaptor -g https://dj32ags.westus.cloudapp.azure.com:6443 -u siteadmin -p &lt;PASSWORD&gt; -a true
</pre>

## Install GeoEvent

<pre>
tar xvzf ArcGIS_GeoEvent_Server_105_154057.tar.gz
cd ArcGISGeoEventServer/
./Setup.sh -m silent -l yes

cd /home/arcgis/server/tools

./authorizeSoftware -f /home/arcgis/Server_GeoEvent.prvc -e &lt;email-address&gt;

/home/arcgis/server/GeoEvent/bin/ArcGISGeoEvent-service start
</pre>

## Install DataStores

<pre>
tar xvzf ArcGIS_DataStore_Linux_105_154054.tar.gz
cd ArcGISDataStore_Linux/

./Setup -m silent -l yes

/home/arcgis/arcgis/datastore/tools/configuredatastore.sh https://dj32ags.westus.cloudapp.azure.com:6443/arcgis/admin siteadmin &lt;PASSWORD&gt; /home/arcgis/arcgis/rds

/home/arcgis/arcgis/datastore/tools/configuredatastore.sh https://dj32ags.westus.cloudapp.azure.com:6443/arcgis/admin siteadmin &lt;PASSWORD&gt; /home/arcgis/arcgis/bds --stores spatiotemporal
</pre>

NOTE: If BDS is on separate server it gets registered to the server running the Relational Data Store.


## Configure Portal

Got to Portal Home

My Organization

Edit Settings

Servers

Federated Servers

Add Server
- https://dj32ags.westus.cloudapp.azure.com/arcgis
- https://dj32ags.westus.cloudapp.azure.com:6443/arcgis

Under Hosting Server

Select Server you just added.

Save

## Configure GeoEvent 

https://dj32ags.westus.cloudapp.azure.com:6143/geoevent/manager/

Site
Data Stores
Register ArcGIS Server

Portal
Use Credentials
- Name: portal
- Username: portaladmin
- Password: &lt;PASSWORD&gt;
- https://dj32ags.westus.cloudapp.azure.com/portal

## Configure Startup Scripts

As root setup the startup scripts. 
<pre>
cp /home/arcgis/arcgis/portal/framework/etc/arcgisportal.service /etc/systemd/system/
cp /home/arcgis/arcgis/datastore/framework/etc/scripts/arcgisdatastore.service /etc/systemd/system/
cp /home/arcgis/server/framework/etc/scripts/arcgisserver.service /etc/systemd/system/
cp /home/arcgis/server/GeoEvent/etc/service/geoevent.service /etc/systemd/system/

systemctl enable arcgisportal.service
systemctl enable arcgisserver.service
systemctl enable arcgisdatastore.service
systemctl enable geoevent.service
</pre>
NOTE: Modify /etc/systemd/system/tomcat8.service arcgisportal.service after network.target on the After line.  
systemctl daemon-reload

<pre>
systemctl reboot
</pre>

After reboot verify that all the services come back.

