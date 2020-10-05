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

def test_data():
	pic = urllib.urlretrieve('http://192.168.2.132:8080/photo.jpg')
	img = Image.open(pic[0])

	nim = img.convert('L')
	nim2 = nim.resize((100,100))

	counter = 0
	pixels = []
	for i in range(100):
		for j in range(100):
			coordinate = x,y = i,j;
			pixelValue = nim2.getpixel(coordinate);
			pixels.append(pixelValue);
			counter = counter+1;

	indata = tuple(pixels[:10000])

	return indata

net = NetworkReader.readFrom('network.xml')

while True:
	result = net.activate(test_data())
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
	
