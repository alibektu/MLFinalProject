import os
from PIL import Image
lst = os.listdir("./train_images")
for file in lst:
	print(str(file))
	im = Image.open("train_images/"+str(file))
	nim = im.convert('L')
	nim2 = nim.resize((100,100))
	nim2.save("train_images/"+str(file))