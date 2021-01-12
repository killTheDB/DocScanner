import os.path
import numpy as np
import cv2
import json
from flask import Flask,request,Response
import uuid

def find_sheet_end_point(window):
    window = window.reshape((4,2))
    new_window = np.zeros((4,2),dtype=np.float32)
    add = window.sum(axis=1)
    diff = np.diff(window,axis=1)
    new_window[0] = window[np.argmin(add)]
    new_window[1] = window[np.argmin(diff)]
    new_window[2] = window[np.argmax(add)]
    new_window[3] = window[np.argmax(diff)]

    return new_window

def ImageToText(image):
    #path to save image after processing
    path_file = ('static/%s.jpg'%uuid.uuid4().hex)
    image = cv2.resize(image,(1300,800))
    original = image
    #image to gray conversion
    gray_image = cv2.cvtColor(image,cv2.COLOR_BGR2GRAY)
    blurred_image = cv2.GaussianBlur(gray_image,(5,5),0)
    edged_image = cv2.Canny(blurred_image,28,48)
    contours,hierarchy = cv2.findContours(edged_image,cv2.RETR_LIST,cv2.CHAIN_APPROX_SIMPLE)
    contours = sorted(contours,key=cv2.contourArea,reverse=True)
    for contour in contours:
        contour_perimeter = cv2.arcLength(contour,True)
        contour_approximation = cv2.approxPolyDP(contour,0.02*contour_perimeter,True)

        if len(contour_approximation)==4:
            selected_window = contour_approximation
            break 
    
    contour_approximation = find_sheet_end_point(selected_window)
    points = np.float32([[0,0],[800,0],[800,800],[0,800]])
    
    perspective = cv2.getPerspectiveTransform(contour_approximation,points)
    wrap = cv2.warpPerspective(original,perspective,(800,800))

    #writing back to path mentioned
    cv2.imwrite(path_file,wrap)
    # cv2.imwrite(path_file,gray_image)
    return json.dumps(path_file)


app = Flask(__name__)
@app.route("/api/upload",methods=['POST'])

def upload():
    # return "TEST"
    # upload image
    img = cv2.imdecode(np.fromstring(request.files['image'].read(),np.uint8),cv2.IMREAD_UNCHANGED)
    img_processed = ImageToText(img)
    return Response(response=img_processed,status=200,mimetype="application/json")


app.run(host="127.0.0.1",port=5000)

if __name__ == "__main__":
    app.run()