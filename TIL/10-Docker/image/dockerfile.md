```shell
# 기본 이미지 선택
# 도커 이미지를 만들 때 바탕이 되는 기본 이미지를 선택 
FROM pytho:3.9

# 작업 디렉토리 설정
WORKDIR /app 

# 3. 필요 패키지 복사
COPY test.txt .

# 4. 패키지 설치
RUN pip install -r test.txt 

# 소스 코드 복사
COPY . .

# 6. 컨테이너 시작 시 실행할 명령어 지정
CMD ["python", "app.py"]
```



이렇게 하면 레이어 5개 



