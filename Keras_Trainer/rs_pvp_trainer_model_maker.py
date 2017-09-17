# -*- coding: utf-8 -*-
"""
Created on Sat Sep 16 21:02:25 2017

@author: InderTheGreat
"""

import tensorflow as tf
import keras as keras
from keras.models import Sequential
from keras.layers.core import Dense, Dropout, Activation
from keras.layers import LSTM, TimeDistributed,Reshape
import time
import numpy as np

def custom_model():
    data_dim = 6
    timesteps = 10
    nb_classes = 3
    
    # expected input data shape: (batch_size, timesteps, data_dim)
    model = Sequential()
    model.add(LSTM(250, return_sequences=True,
                   input_shape=(timesteps, data_dim)))  
    model.add(Activation('tanh'))
    model.add(LSTM(250, return_sequences=True))  
    model.add(Activation('tanh'))
    model.add(LSTM(250, return_sequences=True))  
    model.add(Activation('tanh'))
    model.add(LSTM(250, return_sequences=True)) 
    model.add(Activation('tanh'))
    model.add(LSTM(250, return_sequences=True))  
    model.add(Activation('tanh'))
    model.add(Dense(nb_classes, activation='softmax'))
    
    model.compile(loss='categorical_crossentropy',
              optimizer='adam',
              metrics=['accuracy'])
    
    return model  

def custom_model_dense():
    data_dim = 6
    timesteps = 1
    nb_classes = 3
    
    # expected input data shape: (batch_size, timesteps, data_dim)
    model = Sequential()
    model.add(Dense(150,
                   input_shape=(data_dim,)))  
    model.add(Activation('tanh'))
    model.add(Dense(150))  
    model.add(Activation('tanh'))
    model.add(Dense(150))  
    model.add(Activation('tanh'))
    model.add(Dense(150)) 
    model.add(Activation('tanh'))
    model.add(Dense(150))  
    model.add(Activation('tanh'))
    model.add(Dense(nb_classes, activation='softmax'))
    
    model.compile(loss='categorical_crossentropy',
              optimizer='adam',
              metrics=['accuracy'])
    
    return model  

def make_model(file):
    print("==================================================") 
    
    print("Creating Model At: ",file) 
    start_time = time.time()
    model = custom_model_dense()    
    
    json_model = model.to_json()
    
    with open(file, "w") as json_file:
        json_file.write(json_model)
    
    end_time = time.time()
    total_time = end_time-start_time
    print("Model Created: ",total_time, " seconds")
    
    print("==================================================")
    

if __name__ == "__main__":   
    make_model("rs_pvp_model_desne_1.json")

