

fin = open("../logs/Hosted_SummaryReport.csv")


results = {}

for line in fin:
    parts = line.split(',')
    nm = parts[0]
    rtcode = parts[1]
    size = parts[4]
    ms = int(parts[5])

    if nm in results:
        results[nm].append(ms)
    else:
        results[nm] = []
        results[nm].append(ms)
    
    
for result in results:
    #print((float(sum(results[result])))/float(len(results[result])))
    print(result + ": " + str(1000.0/(float(sum(results[result]))/float(len(results[result])))))

fin.close()
