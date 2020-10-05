from multiprocessing import Pool
from PIL import Image
import urllib
import os

def test_d():
	pic = urllib.urlretrieve('http://192.168.2.132:8080/photo.jpg')
	img = Image.open(pic[0])
	
	box = (0, 120, 320, 240)
	area = img.crop(box)
	area.save('00.jpg', img.format)
	im = Image.open("00.jpg")
	nim = im.convert('L')
	nim2 = nim.resize((100,100))

	counter = 0
	pixels = []
	for i in range(100):
		for j in range(100):
			coordinate = x,y = i,j;
			pixelValue = nim2.getpixel(coordinate);
			pixels.append(pixelValue);
			counter = counter+1;

	file=open("test.data",'r')
	indata = tuple(pixels[:10000])

	return indata


print (test_d())
