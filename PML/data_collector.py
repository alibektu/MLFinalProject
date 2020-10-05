from nxt.locator import *
from nxt.sensor import *
from nxt.motor import *
import time
import threading
import curses
import nxt
import urllib
from PIL import Image
import time

b = find_one_brick()
b.play_tone(200,200)

left_motor = Motor(b,PORT_B)
right_motor = Motor(b,PORT_A)

#synchronize the motors
forw = nxt.SynchronizedMotors(right_motor,left_motor,0)
lef_ = nxt.SynchronizedMotors(left_motor,right_motor,20)
rig_ = nxt.SynchronizedMotors(right_motor,left_motor,20)


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

def back():  #TODO(alibek): has to be fixed using sync
	cur_ins("back")
	left_motor.run(-30)
	right_motor.run(-30)
	time.sleep(1)
	left_motor.brake()
	right_motor.brake()

def left():
	cur_ins("left")
	left_motor.turn(30, 30)
	right_motor.turn(-30,30)
	
def right():
	cur_ins("right")
	left_motor.turn(-30, 30)
	right_motor.turn(30,30)


def capture(x):
	pic = urllib.urlretrieve('http://192.168.2.132:8080/photo.jpg')
	im = Image.open(pic[0])
	name = str(x)+".jpg"
	im.save("train_images/"+name)
	b.play_tone(200,200)  #Tells us image processing is done
	time.sleep(3)


def key_collector(win):
	file = open("instr_1.data",'w')
	win.nodelay(True)
	x=0
	while True:
		win.clear()
		#win.addstr(0,0,str(x))

		key=""
		try:
			key = win.getkey()
		except:
			key = ""
		if key ==" ":
			break
		elif str(key) == "KEY_UP":
			capture(x)
			forward()
			file.write(str(key))
			file.write("\n")
			x=x+1
			key = ""
		elif str(key) == "KEY_DOWN":
			capture(x)
			back()
			file.write(str(key))
			file.write("\n")
			x=x+1
			key = ""
		elif str(key) == "KEY_LEFT":
			capture(x)
			left()
			file.write(str(key))
			file.write("\n")
			x=x+1
			key = ""
		elif str(key) == "KEY_RIGHT":
			capture(x)
			right()
			file.write(str(key))
			file.write("\n")
			x=x+1
			key = ""
	file.close()





#Run below for execution
curses.wrapper(key_collector)
