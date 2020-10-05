from pybrain.tools.shortcuts import buildNetwork
from pybrain.datasets import SupervisedDataSet
from pybrain.supervised.trainers import BackpropTrainer
import os
from PIL import Image
from pybrain.tools.xml.networkreader import NetworkReader
from pybrain.tools.xml.networkwriter import NetworkWriter

from multiprocessing import Pool
import urllib
from nxt.locator import *
from nxt.sensor import *
from nxt.motor import *
import time
import threading
import curses
import nxt


b = find_one_brick()
b.play_tone(200,200)

m_lef = Motor(b,PORT_B)
m_rig = Motor(b,PORT_A)

#synchronize the motors
forw = nxt.SynchronizedMotors(m_rig,m_lef,0)
lef_ = nxt.SynchronizedMotors(m_lef,m_rig,20)
rig_ = nxt.SynchronizedMotors(m_rig,m_lef,20)


current_instruction=""

def cur_ins(instruction):
	global current_instruction
	current_instruction=instruction

def printI():
	global current_instruction
	print (current_instruction)
	
def forward():
	cur_ins("forward")
	forw.turn(70,40)
'''	m_lef.run(30)
	m_rig.run(30)
	time.sleep(1)
	m_lef.brake()
	m_rig.brake()
'''	

def back():  #has to be fixed using sync
	cur_ins("back")
	m_lef.run(-30)
	m_rig.run(-30)
	time.sleep(1)
	m_lef.brake()
	m_rig.brake()

def left():
	cur_ins("left")
	m_lef.turn(30, 30)
	m_rig.turn(-30,30)
	
def right():
	cur_ins("right")
	m_lef.turn(-30, 30)
	m_rig.turn(30,30)

def test_d():
	pic = urllib.urlretrieve('http://192.168.2.132:8080/photo.jpg')
	img = Image.open(pic[0])
	
	#box = (0, 120, 320, 240)
	#area = img.crop(box)
	#area.save('00.jpg', img.format)
	#im = Image.open("00.jpg")
	nim = img.convert('L')
	nim2 = nim.resize((100,100))

	#file_ = open("test_pixel_data.txt",'w')

	counter = 0
	pixels = []
	for i in range(100):
		for j in range(100):
			coordinate = x,y = i,j;
			pixelValue = nim2.getpixel(coordinate);
			pixels.append(pixelValue);
			counter = counter+1;
			#file_.write(str(pixels[counter-1])+" ")
	#file_.write("\n")

	file=open("test.data",'r')
	indata = tuple(pixels[:10000])

	#nim2.save("00.jpg")
	
	return indata
	#fd_img.close()

net = NetworkReader.readFrom('network.xml')

'''
file=open("test.data",'r')
line1=file.readlines()
line = line1[0];
data_p = [float(x) for x in line.strip().split(" ") if x!=""]
indata = tuple(data_p[:10000])
#print (indata)
'''


while True:
	result = net.activate(test_d())
	predicted_inst = result.argmax()
	print(predicted_inst)
	if predicted_inst == 0:
		forward()
	elif predicted_inst == 1:
		back()
	elif predicted_inst == 2:
		left()	
	elif predicted_inst == 3:
		right()
	
