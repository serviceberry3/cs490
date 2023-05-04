import csv
import argparse
import matplotlib.pyplot as plt



def plot(file):
    with open(file, "r+", encoding="utf-8") as csv_file:
        content = csv_file.read()

    with open(file + "_v2", "w+", encoding="utf-8") as csv_file:
        csv_file.write(content.replace('"', ''))

    with open(file + "_v2") as f: 
        lines = csv.reader(f, delimiter = ',') 
  
        for line in lines:
            #print("line is", line[0], line[1], line[2])
            plt.plot(float(line[1]), float(line[2]), marker="o", markersize=5, markeredgecolor="red", markerfacecolor="green")
            plt.text(float(line[1]), float(line[2]), str(line[0]))

    plt.show()



if __name__ == "__main__":
    parser = argparse.ArgumentParser()
    parser.add_argument("filename")
    args = parser.parse_args()

    plot(args.filename)