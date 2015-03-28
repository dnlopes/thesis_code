#!/bin/bash

BASE_DIR="../"
#print variable on a screen

cd ../ImgGen/ImgFiles; make clean ; make ; cd ../../populate ; ./populate_images

