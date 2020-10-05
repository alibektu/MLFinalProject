#include <stdlib.h>
#include <iostream>
#include <fstream>
#include <iomanip>

using namespace std;

const KEY_UP = "KEY_UP";
const KEY_DOWN = "KEY_DOWN";
const KEY_RIGHT = "KEY_RIGHT";
const KEY_LEFT = "KEY_LEFT";

const KEY_UP_VALUE = "1 0 0 0";
const KEY_DOWN_VALUE = "0 1 0 0";
const KEY_RIGHT_VALUE = "0 0 0 1";
const KEY_LEFT_VALUE = "0 0 1 0";

const instructionsFile = "instr_1.data";
const pixedDataFile = "pixel_data_1.txt";
const mappedInstructionsFile = "parse_int_1.data";

int main() {
	ifstream inFil;
	ifstream pinFile;
	ofstream ofFile;
	inFile.open(instructionsFile, ios::in);
	pinFile.open(pixedDataFile, ios::in);
	ofFile.open(mappedInstructionsFile, ios::out);

	while(!inFile.eof() && !pinFile.eof()) {
		for (int i=0;!pinFile.eof();i++) {
			string pixel;
			pinFile>>pixel;
			if (pixel != "")
				ofFile<<pixel<<" ";
		}

		string instr = "";
		inFile>>instr;
		if (instr!="") {
			if (instr == KEY_UP)
				ofFile<<KEY_UP_VALUE<<endl;
			else if (instr == KEY_DOWN)
				ofFile<<KEY_DOWN_VALUE<<endl;
			else if (instr == KEY_RIGHT)
				ofFile<<KEY_RIGHT_VALUE<<endl;
			else if (instr == KEY_LEFT)
				ofFile<<KEY_LEFT_VALUE<<endl;
		}
	}

	inFile.close();
	pinFile.close();
	ofFile.close();
}