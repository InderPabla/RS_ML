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
from keras.utils.vis_utils import plot_model
import graphviz
import pydot

def custom_model_2():
    data_dim = 3
    timesteps = 10
    nb_classes = 3
    
    # expected input data shape: (batch_size, timesteps, data_dim)
    model = Sequential()
    model.add(LSTM(250, return_sequences=True,
                   input_shape=(timesteps, data_dim)))  
    print(model.output_shape)
    model.add(Activation('tanh'))
    print(model.output_shape)
    model.add(LSTM(250, return_sequences=True))  
    model.add(Activation('tanh'))
    print(model.output_shape)
    model.add(LSTM(250, return_sequences=True))  
    model.add(Activation('tanh'))
    print(model.output_shape)
    model.add(LSTM(250, return_sequences=True)) 
    model.add(Activation('tanh'))
    print(model.output_shape)
    model.add(LSTM(250, return_sequences=True))  
    model.add(Activation('tanh'))
    print(model.output_shape)
    model.add(Activation('tanh'))
    model.add(keras.layers.core.Flatten())
    print(model.output_shape)
    model.add(Activation('tanh'))
    model.add(Dense(nb_classes,activation='softmax',input_shape=(2500,)))
    print(model.output_shape)
    model.compile(loss='categorical_crossentropy',
              optimizer='adam',
              metrics=['accuracy'])
    
    return model  

def custom_model():
    data_dim = 6
    timesteps = 10
    nb_classes = 3
    
    # expected input data shape: (batch_size, timesteps, data_dim)
    model = Sequential()
    model.add(LSTM(250, return_sequences=True,
                   input_shape=(timesteps, data_dim)))  
    print(model.output_shape)
    model.add(Activation('tanh'))
    print(model.output_shape)
    model.add(LSTM(250, return_sequences=True))  
    model.add(Activation('tanh'))
    print(model.output_shape)
    model.add(LSTM(250, return_sequences=True))  
    model.add(Activation('tanh'))
    print(model.output_shape)
    model.add(LSTM(250, return_sequences=True)) 
    model.add(Activation('tanh'))
    print(model.output_shape)
    model.add(LSTM(250, return_sequences=True))  
    model.add(Activation('tanh'))
    print(model.output_shape)
    model.add(Activation('tanh'))
    model.add(keras.layers.core.Flatten())
    print(model.output_shape)
    model.add(Activation('tanh'))
    model.add(Dense(3,activation='softmax',input_shape=(2500,)))
    print(model.output_shape)
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

def custom_model_dense_2():
    data_dim = 3
    timesteps = 1
    nb_classes = 3
    
    # expected input data shape: (batch_size, timesteps, data_dim)
    model = Sequential()
    model.add(Dense(512,
                   input_shape=(data_dim,)))   
    model.add(Activation('tanh'))
    model.add(Dense(512))  
    model.add(Activation('tanh'))
    model.add(Dense(256))  
    model.add(Activation('tanh'))
    model.add(Dense(256))  
    model.add(Activation('tanh'))
    model.add(Dense(128))  
    model.add(Activation('tanh'))
    model.add(Dense(128))  
    model.add(Activation('tanh'))
    model.add(Dense(64))  
    model.add(Activation('tanh'))
    model.add(Dense(64))  
    model.add(Activation('tanh'))
    model.add(Dense(32))  
    model.add(Activation('tanh'))
    model.add(Dense(32))  
    model.add(Activation('tanh'))
    model.add(Dense(16))  
    model.add(Activation('tanh'))
    model.add(Dense(16))  
    model.add(Activation('tanh'))
    model.add(Dense(8))  
    model.add(Activation('tanh'))
    model.add(Dense(8))  
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
    model = custom_model_dense_2()    
    plot_model(model, to_file='model.png', show_shapes=True)
    json_model = model.to_json()
    
    with open(file, "w") as json_file:
        json_file.write(json_model)
    
    end_time = time.time()
    total_time = end_time-start_time
    print("Model Created: ",total_time, " seconds")
    
    print("==================================================")
    

if __name__ == "__main__":   
    make_model("rs_pvp_model_2.json")

