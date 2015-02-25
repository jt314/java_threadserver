default: all

all:
	javac -Xlint:unchecked cs455/scaling/*/*.java

clean:
	rm cs455/scaling/*/*.class

pack:
	tar -cvf Todd_Jonathon_ASG2.tar cs455/scaling/*/*.java makefile

