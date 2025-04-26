>자바스크립트는 기본적으로 **비동기 처리**를 위한 기능을 제공합니다. 그중에서도 `Promise`는 대표적인 비동기 처리 객체로, **콜백 지옥(callback hell)** 문제를 해결할 수 있도록 도와줍니다.

---

## ✅ Promise란?

`Promise`는 **비동기 작업의 최종 완료 또는 실패를 나타내는 객체**입니다. 작업이 성공하면 `resolve`, 실패하면 `reject`를 호출하며 상태를 바꿉니다.

### 상태(state)
- `pending` (대기): 아직 결과가 정해지지 않은 초기 상태
- `fulfilled` (성공): `resolve(value)`가 호출된 상태
- `rejected` (실패): `reject(error)`가 호출된 상태

### 결과(result)
- 상태가 `fulfilled`면 `result`는 성공값(value)
- 상태가 `rejected`면 `result`는 에러(error)
- `pending` 상태에선 `result`는 `undefined`

---

## ✅ Promise 기본 문법

```js
const executor = (resolve, reject) => {
    // 비동기 처리 로직
};

const promise = new Promise(executor);
```

- `executor`는 반드시 전달되어야 하며, `resolve`, `reject`를 인자로 받습니다.
- `Promise` 객체가 생성되면 `executor`는 자동으로 실행됩니다.

---

## ✅ 사용 예제

```js
const executor = (resolve, reject) => {
    setTimeout(() => {
        resolve('성공');
        reject('실패'); // 주의: 호출되더라도 무시됨 (resolve가 먼저 호출되었기 때문)
    }, 3000);
};

const promise = new Promise(executor);

promise.then(
    (result) => {
        console.log(result);  // 성공 시
    },
    (error) => {
        console.log(error);  // 실패 시
    }
);

// 또는 체이닝 방식으로 처리
promise
    .then((result) => {
        console.log(result);
    })
    .catch((error) => {
        console.log(error);
    });
```

### ❗ 주의할 점
- `resolve` 또는 `reject` 중 하나만 호출되어야 하며, **둘 다 호출될 경우 첫 번째만 반영**됩니다.

---

## ✅ 왜 Promise를 사용하는가?

### 콜백 지옥(callback hell) 문제
```js
// 전통적인 콜백 방식
login(user, (userInfo) => {
    getData(userInfo, (data) => {
        process(data, (result) => {
            console.log(result);
        });
    });
});
```

이처럼 콜백이 중첩되면 가독성이 떨어지고 에러 처리도 복잡합니다. `Promise`를 사용하면 다음과 같이 개선할 수 있습니다:

### Promise 체이닝
```js
login(user)
    .then(getData)
    .then(process)
    .then(console.log)
    .catch(console.error);
```

- 가독성이 높아지고 에러도 `.catch()` 하나로 처리 가능

---

## ✅ Promise.all
- 여러 개의 Promise를 동시에 실행하고 **모두 성공했을 때만** 처리

```js
const p1 = Promise.resolve(1);
const p2 = Promise.resolve(2);
const p3 = Promise.resolve(3);

Promise.all([p1, p2, p3])
    .then(results => console.log(results)) // [1, 2, 3]
    .catch(error => console.error(error)); // 하나라도 실패하면 catch
```

---

## ✅ Promise 메서드들
| 메서드 | 설명               |
|-----|------------------|
| `then(onFulfilled, onRejected)`    | 성공 시 실행          |
|  `catch(onRejected)`   |        실패 시 처리          |
|  `catch(onRejected)`   | 성공/실패 관계없이 마지막에 실행 |


```js
promise
    .then(result => console.log(result))
    .catch(error => console.error(error))
    .finally(() => console.log('작업 완료'));
```

---

## ✨ 결론

- `Promise`는 자바스크립트의 비동기 처리에 있어 필수적인 요소입니다.
- 콜백 지옥을 피하고, 더 읽기 쉬운 코드를 작성할 수 있도록 도와줍니다.
- `async/await`의 기반이 되는 개념이므로 반드시 이해하고 있어야 합니다.

---

