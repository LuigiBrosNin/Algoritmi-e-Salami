##
# source directory
##
SRC_DIR := mnkgame

##
# output directory
##
OUT_DIR := bin

##
# sources
##
SRCS := $(wildcard $(SRC_DIR)/*.java)

##
# classes
## 
CLS := $(SRCS:$(SRC_DIR)/%.java=$(OUT_DIR)/%.class)

##
# compiler and compiler flags
##
JC := javac
JCFLAGS := -d $(OUT_DIR)/ -cp $(SRC_DIR)/

##
# suffixes
##
.SUFFIXES: .java

##
# targets that do not produce output files
##
.PHONY: all clean

##
# default target(s)
##
all: $(CLS)

$(CLS): $(OUT_DIR)/%.class: $(SRC_DIR)/%.java
    $(JC) $(JCFLAGS) $<

##
# clean up any output files
##
clean:
    rm $(OUT_DIR)/*.class
make :
	javac -cp ".." *.java

play :
	java -cp ".." mnkgame.MNKGame 3 3 3 mnkgame.RandomPlayer

test :
	java -cp ".." mnkgame.MNKGame 5 5 4 mnkgame.RandomPlayer mnkgame.QuasiRandomPlayer
