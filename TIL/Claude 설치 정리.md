# Mac Claude Code 설치 & 설정 정리

1. 설치
```
curl -fsSL https://claude.ai/install.sh | bash
```

2. PATH 설치 

```
echo 'export PATH="$HOME/.local/bin:$PATH"' >> ~/.zshrc && source ~/.zshrc
```
- 터미널이 `claude` 명령어를 찾을 수 있게 경로 추가
- 안 하게 될 경우 "command not found" 에러 발생
- .zshrc 파일에 설정 추가 → 터미널 시작할 때마다 자동 적용
- source ~/.zshrc → 지금 바로 적용

3. 실행 & 로그인
```
claude
```

- 로그인 방식 선택: 1. Claude account with subscription
  - claude.ai 계정으로 로그인
- 터미널 설정: 1. Yes, use recommended settings 
  - Option+Enter로 줄바꿈, 시각적 알림 등
- 작업 폴더 확인: 1. Yes, I trust this folder
  - Claude가 현재 폴더의 파일을 읽고 수정할 수 있게 권한 부여