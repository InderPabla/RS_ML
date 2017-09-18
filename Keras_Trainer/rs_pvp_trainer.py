import tensorflow as tf
import keras as keras

from keras.models import model_from_json
from keras.optimizers import SGD, Adam, RMSprop
import os
import os.path
import copy
import numpy as np
import socket
import struct
import time

def get_raw_data(count, raw_file, raw_input_size, raw_output_size):
    raw_X = []
    raw_Y = []
    raw_count = 0
    
    with open(raw_file) as file:
        for line in file: 
            numbers_str = line.split(',')
            numbers_float = [float(x) for x in numbers_str] 

            X = []
            Y = []
            
            for i in range(0, raw_input_size):
                X.append(numbers_float[i])

            for i in range(raw_input_size, len(numbers_float)):
                Y.append(numbers_float[i])
            
            raw_X.append(X)
            raw_Y.append(Y)
            
            raw_count = raw_count + 1
            
            if raw_count == count:
                break;
                
    raw_X = np.array(raw_X, dtype = np.float32) 
    raw_Y = np.array(raw_Y, dtype = np.float32) 
    return raw_X, raw_Y

def parseDataSet(data_set_path, raw_max_count):
    data_x = []
    data_y = []
    time_step = 10
    max_a = 0
    time_data_x = []
    with open(data_set_path) as file:
        for line in file: 
            numbers_str = line.split(',')
            numbers_float = [float(x) for x in numbers_str]
            x = numbers_float[0:6]
            y = numbers_float[6:9]
            
            if(len(time_data_x)==0):
                for i in range(0,time_step):
                    time_data_x.append(x)
                
            y = np.array(y)
            data_y.append(y)
  
            time_data_x.append(x)
            if(len(time_data_x)>time_step):
                time_data_x.pop(0)

            data_x.append(copy.deepcopy(time_data_x))

            max_a = max_a+1
            if(max_a == raw_max_count):
                break
            

    return np.array(data_x), np.array(data_y)

model_name = "rs_pvp_model_1.json"
save_weights_file = "rs_pvp_weights.h5"

model_name_dense = "rs_pvp_model_desne_1.json"
save_weights_dense_file = "rs_pvp_dense_weights.h5"

data_set_1 = "Data_Sets/fighting_data_set_1.txt"
data_set_2 = "Data_Sets/fighting_data_set_2.txt"
data_set_3 = "Data_Sets/fighting_data_set_3.txt"
data_set_4 = "Data_Sets/fighting_data_set_4.txt"
data_set_5 = "Data_Sets/fighting_data_set_5.txt"

json_file = open(model_name, 'r')
loaded_model_json = json_file.read()
json_file.close()

model = model_from_json(loaded_model_json)

if(os.path.exists(save_weights_file)):
    print("already exsits")
    model.load_weights(save_weights_file) 
else:
    print("does not exist")

opt = SGD(lr=0.001, decay=0.000001, momentum=0.9, nesterov=True)
model.compile(loss = "mean_squared_error", optimizer = opt)

traning_mode = False
predict_mode = True

actions = ["COMBO","EAT","NONE"]

if(traning_mode == True):
    print("Traning Mode")
    data_x_1, data_y_1 = parseDataSet(data_set_1,12000)
    data_x_2, data_y_2 = parseDataSet(data_set_2,8900)
    data_x_3, data_y_3 = parseDataSet(data_set_3,11000)
    data_x_4, data_y_4 = parseDataSet(data_set_4,11000)
    data_x_5, data_y_5 = parseDataSet(data_set_5,11000)

    
    for i in range(0,25):
        model.fit(np.array(data_x_1), np.array(data_y_1), nb_epoch=1,verbose = 2)
        model.fit(np.array(data_x_2), np.array(data_y_2), nb_epoch=1,verbose = 2)
        model.fit(np.array(data_x_3), np.array(data_y_3), nb_epoch=1,verbose = 2)
        model.fit(np.array(data_x_4), np.array(data_y_4), nb_epoch=1,verbose = 2)
        model.fit(np.array(data_x_5), np.array(data_y_5), nb_epoch=1,verbose = 2)
        model.save_weights(save_weights_file) 
        
    print("Exiting")
    '''
    data_x_useless_1, data_y_1 = get_raw_data(12000, data_set_1, 6, 3)
    data_x_useless_2, data_y_2 = get_raw_data(8900, data_set_2, 6, 3)
    data_x_useless_3, data_y_3 = get_raw_data(11000, data_set_3, 6, 3)
    data_x_useless_4, data_y_4 = get_raw_data(11000, data_set_4, 6, 3)
    data_x_useless_5, data_y_5 = get_raw_data(11000, data_set_5, 6, 3)
    
    for i in range(0,25):
        model.fit(np.array(data_x_useless_1), np.array(data_y_1), nb_epoch=1,verbose = 2)
        model.fit(np.array(data_x_useless_2), np.array(data_y_2), nb_epoch=1,verbose = 2)
        model.fit(np.array(data_x_useless_3), np.array(data_y_3), nb_epoch=1,verbose = 2)
        model.fit(np.array(data_x_useless_4), np.array(data_y_4), nb_epoch=1,verbose = 2)
        model.fit(np.array(data_x_useless_5), np.array(data_y_5), nb_epoch=1,verbose = 2)
        model.save_weights(save_weights_dense_file) 
    '''
else: 
   if(predict_mode == True):
       print("Prediction Mode")
       
       #data_x_1, data_y_1 = get_raw_data(12000, data_set_1, 6, 3)
       data_x_1, data_y_1 = parseDataSet(data_set_1,12000)
       pre = model.predict(np.array(data_x_1))
       correct = 0
       for i in range(0,len(pre)):
           if(np.argmax(pre[i]) == np.argmax(data_y_1[i])):
               if(np.argmax(pre[i]) == 1 and data_x_1[i][9][0]==-1):
                   print(data_x_1[i][9][0],"  ",pre[i]) 
               if(np.argmax(pre[i]) == 1):
                   print("Yes")     
               correct = correct +1
       print(correct)
   else:
       print("Testing Mode") 
       clientsocket = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
       clientsocket.connect(('localhost', 5050))
       time_step = 10
       time_data_x = []
       while(True):
           expected_length = 6*4
           recieved_length = 0
           data = []
    
           while(recieved_length  < expected_length ):
               data = clientsocket.recv(expected_length)
               recieved_length += len(data)

           real_data = []   

           if(len(data) == expected_length):
               real_data = []   
               a,b,c,d,e,f = struct.unpack('ffffff', bytearray(bytes(data)))
               real_data = np.array([a,b,c,d,e,f ],dtype=np.float32)
               
               if(len(time_data_x)==0):
                   for i in range(0,time_step):
                       time_data_x.append(real_data)
                

               time_data_x.append(real_data)
               if(len(time_data_x)>time_step):
                   time_data_x.pop(0)
                    
               index = np.argmax(model.predict(np.array([time_data_x]))[0]*100)
               message = str(index)
               print(actions[index])
               clientsocket.send(message.encode())
                 
               '''
               index = np.argmax(model.predict(np.array([real_data]))[0]*100)
               message = str(index)
               print(message)
               clientsocket.send(message.encode())
               #print(actions[index])
               '''
               
       
            
           






























