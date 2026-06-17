import cv2
cap=cv2.VideoCapture(1,cv2.CAP_DSHOW)
# cap.set(cv2.CAP_PROP_FOURCC, cv2.VideoWriter_fourcc('M', 'J', 'P', 'G'))
# cap.set(cv2.CAP_PROP_FRAME_WIDTH, 640)
# cap.set(cv2.CAP_PROP_FRAME_HEIGHT, 480)
while cap.isOpened():
    ret, frame = cap.read()
    if not ret:          # 读帧失败就退出
        break
    cv2.imshow("win", frame)
    # 1 ms 等待按键，同时让 GUI 刷新
    if cv2.waitKey(1) & 0xFF == ord('q'):
        break