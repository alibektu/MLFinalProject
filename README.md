# Machine Learning Final Project


This project is an attempt at implementing autonomous driving in low cost LEGO MINDSTORM robots. We seek to train a neural network through back propagation with input as terrain images captured from android devices and output as a one of four basic commands that are in turn used in controlling the movements of the robot.

# Motivation
The motivation for this project stems from the work of proposed in ALVIN by Dean A. Pomerleau in his paper Neural Network Vision for Autonomous Driving. In this paper, he proposes a simple feed forward back propagation multilayer perception artificial neural network with image inputs and 30 outputs for driving a modified military truck. His success provides the motivation for the work herein described.

# Background
In recent times, there have been much interest and curiosity in the field of machine learning that concerns itself with autonomous driving. The work being done at Google and Tesla have provided the media coverage and attention the field required. Majority of the projects undertaken by these companies are expensive and high end in terms of the technology used. Besides Google and Tesla, other companies as Baidu, Audi, Honda and recently Apple have also entered into the race for fully autonomous vehicles. The importance of autonomous driving cannot be underestimated as it eliminates majority of the problems associated with traditional driving.

Safety issues on major roads have been one of the many motivations pushing the industry. Also the media appeal that comes with autonomy makes it an interesting concept to the general public. One of the major problems with the development of such a system has been the cost. In this paper, we describe an easy, less expensive achievement of autonomy through the use of NXT’s Lego Mindstorms. Also open source python libraries for PyBrain and OpenCV are utilized.

# Robot Construction and Build

<img src="https://github.com/alibektu/ML_Final_Project/blob/main/imgs/robot_build.png" width="400">

In this project we use the NXT 2.0 which comes packaged with sonar, sound and light sensor. Also there are two electric motors and Lego bricks for building various designs and architectures. The programmable unit is the NXT brick which is a 32-bit ARM7 microprocessor. It provides 26 Kbytes of FLASH memory and 64 Kbytes of RAM. Also, there is hardware for Bluetooth wireless communication.

<img src="https://github.com/alibektu/ML_Final_Project/blob/main/imgs/nxt.png" width="400">

There are four input ports, three output ports and USB 2.0 port. The brick also comes with a 60x100 pixel dot matrix display, and 8 KHz speaker and a lithium battery with a power adapter. The figure above shows an NXT brick. We constructed an NXT robot with the only requirement of being capable of mounting an android device on top. The finished design is shown below.

<img src="https://github.com/alibektu/ML_Final_Project/blob/main/imgs/robot_design.png" width="400">

# Track Construction
To simulate a road network, we use A4 sheets to construct single and double lanes. As will be shown later, this construction provides a design simplification during the data processing stage.

<img src="https://github.com/alibektu/ML_Final_Project/blob/main/imgs/track.png" width="400">

The images above show the tracks that were used for testing the system after training.

# Robot Control
NXT comes with RobotC as the default programming platform. This unfortunately is not suited to for our project hence we need an alternative. Various third party implementations of RobotC have been made available such as Lejos and nxt-python. We use nxt-python since its quite simple and straight forward to use with minimal setup time. NXT-Python is build in python and provides all the capabilities and flexibility of python as well as similar functions for controlling the nxt brick as RobotC. In nxt- python, bricks may be controlled using either one of two methods. The first uses USB while the second utilizes wireless Bluetooth connection. In this project we use the Bluetooth option since it allows for more flexibility and more suited for the purpose of this project. In particular, this project utilizes just a small section of nxt-python for Bluetooth and motor controls. The following short functions demonstrates our usage of nxt-python. For movement, we use:
```
def forward():
	cur_ins("forward")
	forw.turn(70,40)

def back():
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
```
Although these functions are sufficient for robot control, the motors have not been programmatically related in anyway. This does not allow for finer more controlled movement of the robot. To achieve better robot control we need to synchronize both motors by setting one as the master and the other as the slave. This way we can programmatically specify the relationship between the slave and master robots. The python code for doing this is shown below.
```
#synchronize the motors
forward = nxt.SynchronizedMotors(right_motor,left_motor,0)
left = nxt.SynchronizedMotors(left_motor,right_motor,20)
right = nxt.SynchronizedMotors(right_motor,left_motor,20)
```
At this point we have almost all the nxt-python functionality we will be using except for that for establishing Bluetooth connection. This is also quite simple. The code shown below requires pyBluez or some equivalent library to work.
```
#Locate the NXT device
b = find_one_brick()
#Beep to show device has been connected
b.play_tone(200,200)
```
In the code above, a tone is played upon the establishment of a Bluetooth connection between the control device(computer) and nxt brick. Now all the setup so we can initialize our nxt device as shown below:

```
#initialization of device motors
left_motor = Motor(b,PORT_B)
right_motor = Motor(b,PORT_A)
```

# NEURAL NETWORK ARCHITECUTRE
The neural network used for this project is a simple feed forward back propagation neural network. There are 10000 input neurons for image pixels, 100 hidden neurons (this was arbitrarily chosen. 64 also gives some good result), and four output neurons for controlling the robot either in the forward, backwards, left or right direction. The network is created using the PyBrain library which is a python machine learning platform for building various neural network architectures. The image below, adapted

<img src="https://github.com/alibektu/ML_Final_Project/blob/main/imgs/neural_net.png" width="400">

form ALVIN shows the neural network and input structure.
To create the neural network is quite simple in PyBrain. We have to specify the number of input, output, and hidden neurons and layers. Also, we have the specify the type of learning that will be used. In out case, we use supervised learning. This process is shown below:
```
#create a neural network with 10000 input neurons
#100 hidden neurons and 4 output neurons with bias #neurons included.
net = buildNetwork(10000,100,4,bias=True)

#This defines the structure of our dataset.
#We have 10000 inputs and this will be stored as a tuple together with 4 result data values.
ds = SupervisedDataSet(10000, 4)
```

# Data Collection
To train or use our neural network, we have to obtain data in this case an image of the state of our test or training tack. This where we utilize the android device mounted atop the nxt device. We wrote short python script that grabs the image seen by the android camera from a URL. The application for taking the image is and android application called IP camera which provides an interface to stream video and images from an android device to an IP address. The image capturing process is part of an automated process using a python script that provides a window where the up arrow key represents a forward motion, the down arrow key represents a stop signal and the left and right arrow keys represent left and right turns respectively. When any of these keys are pressed, the following python code captures the most current image and stores it in a predefined directory.
```
pic = urllib.urlretrieve('http://1 92.168.2.132:8080/photo.jpg' )
```

Together with saving the image, the instruction, forward, back, left or right, are also stored in a file which corresponds to the image just save. A snap shot of the instruction file is shown below.
nim = im.resize((100,100))

```
KEY_UP
KEY_UP
KEY_UP
KEY_UP
KEY_UP
KEY_UP
KEY_RIGHT
KEY_LEFT
KEY_LEFT
KEY_UP
KEY_UP
...
```
Later on, this file, together with the images collected will be processed into a form that can be fed into the input nodes of the neural network.
The robot was driven on the training track for about thirty minutes and close to 500 image samples together with labels were collected. Later during training, we found this data was not sufficient hence another 30-minute drive was done to increase the training data pool to just over a thousand. This provided better result during the testing stage.

# Image Processing
The images that were collected in the preceding stage are 320x240. This size is bigger than what we expect to feed into the neural network. Thus in the image processing stage, we use pythons PIL library together with the numpy library to manipulate the images into a desirable form. To show the step involved in the image processing stage, the image below gives a summary.

<img src="https://github.com/alibektu/ML_Final_Project/blob/main/imgs/img_processing.png" width="400">

After image acquisition the next step is to resize the image. Since the input of our neural network is 10000, we resize the image to 100x100.

The resizing operation above may be performed using the following snippet:

```
im = Image.open(“00.jpg”)
nim = im.resize((100,100))
```

<img src="https://github.com/alibektu/ML_Final_Project/blob/main/imgs/resize_img.png" width="400">

At this point, the image is stored in RGB values but we need to represent each RGB value as a single pixel value. This way we can have our 10000 inputs. To do this, again we employ numpy functionalities. Before doing the conversion, were first convert the resized image to a gray scale image.

```
gray = col.convert(‘L’)
bw = np.asarray(gray).copy()
```

Now we have the image as a numpy array stored in bw. The next step is to convert all pixels less than 182 to 0 value and all pixels with values above 182 to 255. This produces the effect shown in the image on the right. 182 was chosen after some experimentation to find a good number that eliminates unwanted environment data. Also because of the use of A4 sheets for the construction of the track, this value is ideal for this project.

<img src="https://github.com/alibektu/ML_Final_Project/blob/main/imgs/gray_img.png" width="400">

At this juncture we have all we need for training the neural network we created earlier. However, since we are using supervised learning, we have to find a way to feed both the input pixels together with the expected output to the training model. To achieve this, we convert the movement instructions to four bit binary numbers. For forward movement we use [1 0 0 0], backwards is [0 1 0 0], left movement is [0 0 1 0] and right turn is [0 0 0 1]. After this conversion, we can write all the training data into a file which can then be passed to the PyBrain model for training. In this file, we first write all the individual pixels into the file and append the expected binary output to the end of the line. Thus a single image together with its output forms a single line.

# Network Training
With all the preprocessing done, the network can be trained after passing all the training data to it. The process is demonstrated in the function shown below. Here we load the data in batches of 10004 each until all our data samples are exhausted. Also when training, we run for a 1000 epochs or until our weights eventually do converge.

```
def load_dataset():
	file=open("parse_int.data",'r')
	for line in file.readlines():
		data_p = [int(x) for x in line.strip().split(" ") if x!=""]
		indata = tuple(data_p[:10000])
		outdata = tuple(data_p[10000:])
		ds.addSample(indata,outdata)
```

After training the network, we save the weights in an xml file so we can reload the network for use easily.

```
#save weights
NetworkWriter.writeToFile(net,'network. xml')
#load instruction
net=NetworkReader.readFrom('network.xml ')
```

# Testing
At this juncture, all the various paths required by our system has been built. The next stage is to test the system. With the neural network already trained, we realized the network could be trained in a relatively short time of less than 10 minutes. The number of epochs used was 500 but this number is quite huge and the error converges quite easily. The graph below shows this result.

<img src="https://github.com/alibektu/ML_Final_Project/blob/main/imgs/result.png" width="400">

From the graph, it can be observed the the final error converges quickly at about 50 epochs. And the error is quite low at this stage. Hence we can save the weights on our network and use them right away. This provides some convenience since should we desire to retrain our network using newly acquired data from some different track or road, we can do so in limited time.

Also, on testing the network on a track different from that used for training, we were able to achieve an accuracy of 96%. Although this number is high, further improvements in data processing and
better construction of road networks can improve the performance further.

The network successfully navigated single and double roads and object and stop sign detections were also accurately detected and the proper control instructions demonstrated.

# Acknowledgement
We will like to express our sincere gratitude to Professor Sung Ju Hwang for the insightful lectures he provided on machine learning during the 2016 First semester period. Also his feedback during the initial stages were extremely helpful.




