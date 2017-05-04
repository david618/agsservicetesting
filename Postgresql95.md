# Installing PostgreSQL for ArcGIS Server

I found [this](http://www.postgresonline.com/journal/archives/362-An-almost-idiots-guide-to-install-PostgreSQL-9.5,-PostGIS-2.2-and-pgRouting-2.1.0-with-Yum.html) to be a good resource.

Here are the steps I took.  Starting with min install of CentOS 7.2.

*Note:* The yum that is part of CentOS 7.2 repo is PostgreSql 9.2
<pre>
yum info postgresql
</pre>

## Install Repo for PostgreSQL 9.5

The repos are [here](https://yum.postgresql.org/repopackages.php)

<pre>
rpm -ivh http://yum.postgresql.org/9.5/redhat/rhel-7-x86_64/pgdg-centos95-9.5-3.noarch.rpm

yum info postgresql95

</pre>

This install version 9.5.6.  This is supported by ArcGIS 10.5; however only 9.5.3 is certified.

## Install PostgreSQL

<pre>
yum install -y postgresql95 postgresql95-server postgresql95-libs postgresql95-contrib postgresql95-devel
</pre>

## Configure Data Folder

I added a second drive to my Azure VM and mounted it to /opt.

<pre>
mkdir /opt/pgdata
chown postgres. /opt/pgdata

vi /etc/sysconfig/pgsql/postgresql-9.5

PGDATA=/opt/pgdata
</pre>

## Init/Start the DB

<pre>
/usr/pgsql-9.5/bin/postgresql95-setup initdb

systemctl start postgresql-9.5.service
systemctl enable postgresql-9.5.service

firewall-cmd --add-service=postgresql
</pre>

## Add Admin Pack (optional)

<pre>
su postgres
/usr/pgsql-9.5/bin/psql -p 5432 -c "CREATE EXTENSION adminpack;"
</pre>

## Install PostGIS and Other Stuff

<pre>
yum install epel-release
yum install -y postgis2_95 postgis2_95-client

yum install -y ogr_fdw95

yum install -y pgrouting_95
</pre>

## Configure for Access

Add line to /var/lib/pgsql/9.5/data/pg_hba.conf

<pre>
host    all     all     192.168.56.0/24 trust
</pre>

You'll need to use whatever IP range you need.

Insert line in /var/lib/pgsql/9.5/data/postgresql.conf

<pre>
listen_addresses = '*'
</pre>

systemctl restart postgresql-9.5

## Configure Postgres User

<pre>
su - postgres
</pre>

Add line to .bash_profile

<pre>
export PATH=$PATH:/usr/pgsql-9.5/bin
</pre>


Set postgres password.

<pre>
psql

alter user postgres password 'PASSWORD';

\q
</pre>

## Configure for ArcGIS 

[Esri installation Instruction](http://desktop.arcgis.com/en/arcmap/10.3/manage-data/gdbs-in-postgresql/setup-geodatabase-postgresql-linux.htm)

Use pg_config command to LIBDIR.  For my installation it was /usr/pgsql-9.5/lib

Copy st_geometry.so (This can be found in ArcGIS Desktop Installation) to /usr/pgsql-9.5/lib

Set the permissions 755 same as other so files.

Restart postgresql.

### Create Data Folder

Created a folder as postgres user.  

<pre>
mkdir /opt/pgdata/gis1
</pre>

### Create Windows Server

**NOTE:** Ran into issues trying to manage via ssh tunnel.  I ended up adding a Windows Virtual Machine to my cluster and installed ArcGIS Desktop.  Small machine DS2v2 (2 cpu, 7g mem).

Installed ArcGIS Desktop

Installed [pgadmin3](https://www.pgadmin.org/)

This is a nice GUI app for administering Postgresql.

From pgAdmin3 connected to database as postgres user using the password you set earlier.

Created a Tablespace "gis1" and pointed to this new folder "/opt/pgdata/gis1" 

### Create Enterprise GeoDatabase 

From ArcCatalog ran Create Enterprise Geodatabase

- Database Platform: PostgreSQL
- Instance: Server name (e.g. a81)
- Database: gis1  (Create a new database named gis1)
- Database Admin: postgres
- DB Admin Password: You set this earlier
- GeoDB Admin Password: Set something you want
- Tablespace: gis1
- Authorization File: &lt;Server Auth File&gt;

After creating the GeoDB.  I opened the database as admin (postgres) in pgAdmin3.  Right click on Extensions and added the "postgis" extension.  Also added the "adminpack" extension.


### Create Database Connection

From ArcCatalog

- Database Platform: PostgreSQL
- Instance: Server Name (e.g. a81)
- Database Authentication 
- Username: postgres
- Password: Use postgres password
- Database: Select gis1

### Create Geodatabase user

From ArcCatalog ran Created user (e.g. user1)

- Input Database Connection: Select the database connection you just created
- Datbase User: For example user1
- Database User Password: Password to be set for user1
- Leave Role blank

### Create Database Connection (user1)

- Database Platform: PostgreSQL
- Instance: Server Name (e.g. a81)
- Database Authentication 
- Username: user1
- Password: user1's password
- Database: Select gis1


Now you can create feature classes as user1.

## Created a Table 

From pgadmin3 create a connection to database as user1.

- Name: a81_user1
- Host: a81
- Port: 5432
- Maintenance DB: postgres
- Username: user1
- Password: The one you set earlier.

Click on hte gis Database.  Click on Execute SQL tool.  Looks like a magnify class with SQL.  

<pre>
CREATE TABLE ellipse (
    oid integer,
    a double precision,
    b double precision,
    clat double precision,
    clon double precision,
    rot double precision,
    num integer
);

SELECT AddGeometryColumn('', 'ellipse','geom',4326,'MULTIPOLYGON',2);
</pre>

Inserted data using SQL like this.

<pre>
INSERT INTO ellipse (oid,a,b,clat,clon,rot,num,geom)
VALUES (
    1,
    0.7297795748794967,
    0.8613080071629333,
    38.52927693167145,
    58.639163032069064,
    185.29473112834762,
    1,
    ST_GeomFromGeoJSON('{"coordinates":[[[[58.638249418867,38.521564049539],[58.639323402944,38.521549853032],[58.640385124573,38.521660555016],[58.641588259331,38.521941280906],[58.642705676285,38.522364485245],[58.643714371895,38.522904552674],[58.644601942186,38.523535331805],[58.645364592191,38.524233278562],[58.646004374587,38.524979100504],[58.646526516914,38.525758148605],[58.646937281639,38.526560009259],[58.647242446414,38.527377701086],[58.647446318692,38.528206743968],[58.647551145856,38.529044227672],[58.647556797337,38.529887912864],[58.647460648058,38.530735342507],[58.647257656234,38.531582918206],[58.646940690688,38.532424908553],[58.646501228782,38.533252394766],[58.645930592356,38.534052248603],[58.645221874935,38.534806371531],[58.644372590957,38.535491592798],[58.643387768495,38.536080729663],[58.642282743754,38.536545210799],[58.641084505759,38.536859191355],[58.640023874725,38.53699349041],[58.63894963135,38.537001433733],[58.63788897549,38.536884694761],[58.636688589531,38.536597497922],[58.635575157951,38.536168811104],[58.63457123158,38.535624359985],[58.633688807882,38.534990246338],[58.63293137577,38.534289864745],[58.632296692562,38.533542318511],[58.631779439712,38.532762070362],[58.631373339947,38.531959374936],[58.631072652178,38.531141091442],[58.630873137695,38.530311616598],[58.630772636243,38.529473815527],[58.630771374672,38.528629919208],[58.630872074472,38.527782414281],[58.631079865059,38.526934967865],[58.631401943953,38.526093423365],[58.631846858378,38.525266855248],[58.632423244752,38.524468586044],[58.633137870689,38.523716927775],[58.633992960442,38.523035245346],[58.634983099989,38.522450837438],[58.636092479691,38.521992244602],[58.637293639544,38.521685086038],[58.638249418867,38.521564049539]]]],"type":"MultiPolygon","crs":{"type":"name","properties":{"name":"EPSG:4326"}}}')
);
</pre>

## Create Map Service 
Added this table to ArcMap and published to ArcGIS Server as Map Service.

You'll need to registered the database.  Be sure the hostname is accessible from both ArcGIS Server.

Per [setup instructions](http://desktop.arcgis.com/en/arcmap/latest/manage-data/gdbs-in-postgresql/setup-geodatabase-postgresql-linux.htm) you'll need to configure SDEHOME and PG_HOME.

Added these to .bash_profile for arcgis user.


<pre>
export SDEHOME=/opt/arcgis/server
export PG_HOME=/usr/pgsql-9.5

export LD_LIBRARY_PATH=$LD_LIBRARY_PATH:$PG_HOME/lib:$SDEHOME/lib
</pre>

## Loaded Data

Wrote a Java App to create 1,000,000 random ellipses.  Added as app in [Simulator](https://github.com/david618/Simulator).

Doing a commit every 1,000 inserts the load rate was about 600/s.

Using 4 instances the rate was about 2,400/s.









