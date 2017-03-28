import random


n = 8

i = 0

while i < n:

    i += 1
    
    filename = r"bbox_samples" + str(i) + ".txt"

    fout = open(filename, "w")

    num_samples = 10000

    cnt = 0

    while cnt < num_samples:
        
        lon = random.uniform(-120,-60)
        lat = random.uniform(15,60)

        maxlonsize = 180 - lon;
        maxlatsize = 90 - lat;

        maxsize = min(maxlonsize,maxlatsize)
        
        size = random.uniform(0.1,maxsize)

        lllon = str(lon)
        lllat = str(lat)
        urlon = str(lon + size)
        urlat = str(lat + size)

        bbox = lllon + "," + lllat + "," + urlon + "," + urlat + "\n"

        fout.write(bbox)
        cnt += 1

    fout.close()
