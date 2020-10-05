from pybrain.tools.shortcuts import buildNetwork
from pybrain.datasets import SupervisedDataSet
from pybrain.supervised.trainers import BackpropTrainer
import os
from PIL import Image
from pybrain.tools.xml.networkreader import NetworkReader
from pybrain.tools.xml.networkwriter import NetworkWriter


net = buildNetwork(10000,100,4,bias=True)
ds = SupervisedDataSet(10000, 4)


def train(net,ds,p=500):
	trainer = BackpropTrainer(net,ds)
	file_error = open("ErrorData.data",'w')
	print("\nInitial weigths:", net.params)
	max_error = 1e-7
	error, count=1,1000
	while abs(error) >= max_error and count > 0:
		error=trainer.train()
		count = count -1
		print(error)
		file_error.write(str(error))
	print("Final weights: ", net.params)
	print("Error:",error )
	file_error.close()


def load_dataset():
	file=open("parse_int_1.data",'r')
	for line in file.readlines():
		data_p = [int(x) for x in line.strip().split(" ") if x!=""]
		indata = tuple(data_p[:10000])
		outdata = tuple(data_p[10000:])
		ds.addSample(indata,outdata)


load_dataset()

print("Training\n")
train(net,ds,p=500)

#same weights
NetworkWriter.writeToFile(net,'network.xml')

