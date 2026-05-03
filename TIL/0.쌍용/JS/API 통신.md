# JavaScript에서 API 호출하기 - fetch와 async/await 활용

웹 애플리케이션을 만들다 보면, 클라이언트(사용자 측)에서 서버로 데이터를 요청하고, 그 결과를 받아오는 작업이 매우 중요합니다. 이 글에서는 클라이언트와 서버 간 통신의 기본 개념과 함께, 자바스크립트의 `fetch()` 함수와 `async/await` 구문을 활용해 API를 호출하는 방법을 알아보겠습니다.

---

## 📡 클라이언트와 서버의 통신 구조

웹사이트는 **웹 브라우저**(클라이언트)를 통해 **서버**와 통신합니다. 이 과정은 다음과 같은 순서로 진행됩니다:

1. **클라이언트**가 데이터를 요청
2. **서버**는 이 요청을 받아 내부의 **데이터베이스(DB)**에서 데이터를 조회
3. 조회된 데이터를 **서버**가 다시 **클라이언트**에 전달

> 이 통신 과정을 위해 사용하는 것이 바로 **API**입니다.

---

## 🧩 API란?

API(Application Programming Interface)는 클라이언트와 서버가 데이터를 주고받기 위해 사용하는 **인터페이스**입니다.  
API는 보통 **URL 형태의 주소**로 제공되며, 이 주소를 통해 데이터를 요청할 수 있습니다.

---

## 🛠 JavaScript에서 API 호출 - fetch()

자바스크립트에는 API를 호출할 수 있는 내장 함수인 `fetch()`가 있습니다.  
`fetch()`는 비동기 함수로, 데이터를 요청하고 응답을 받는 데 시간이 걸릴 수 있으므로 **Promise 객체**를 반환합니다.

```js
let response = fetch('https://jsonplaceholder.typicode.com/users')
    .then((result) => console.log(result))
    .catch((error) => console.log(error));
```

### 👉 Promise 기반 처리

위 예시는 `.then()`과 `.catch()`를 이용한 Promise 방식의 비동기 처리입니다.  
하지만 더 간결하고 직관적인 방식은 `async/await`을 사용하는 것입니다.

---

## 🔄 async/await을 활용한 API 호출

다음은 `async/await`을 이용해 API를 호출하고, JSON 데이터를 받아오는 코드입니다.

```js
const getData = async () => {
    try {
        let response = await fetch('https://jsonplaceholder.typicode.com/users');
        let data = await response.json(); // JSON 데이터를 JS 객체로 변환
        console.log(data); // 콘솔에 데이터 출력
    } catch (error) {
        console.log(error); // 에러 처리
    }
}

getData();
```

---

## 🧾 JSON 데이터란?

API에서 전달되는 데이터는 보통 **JSON (JavaScript Object Notation)** 형식입니다.  
이는 자바스크립트의 객체와 유사한 구조를 가진 데이터 형식으로, 사람이 읽고 쓰기 쉬운 형태로 데이터를 표현합니다.

> JSON → JavaScript 객체로 변환하려면 `.json()` 메서드를 사용합니다.

---

## ✅ 요약

- API는 클라이언트와 서버가 데이터를 주고받는 인터페이스입니다.
- `fetch()` 함수로 API를 호출할 수 있고, 이는 비동기적으로 작동합니다.
- 응답 데이터는 JSON 형식으로 오며, `.json()` 메서드로 변환해 사용할 수 있습니다.
- `async/await`을 사용하면 비동기 코드가 더욱 깔끔하고 읽기 쉬워집니다.

---

## 🧪 참고 API (테스트용)

- `https://jsonplaceholder.typicode.com/users`  
  → 사용자 정보 리스트를 반환하는 JSON 데이터 제공

---

감사합니다!  
이 글이 자바스크립트에서 API를 호출하는 방법을 이해하는 데 도움이 되었길 바랍니다.