> 자바스크립트에서는 네트워크 요청처럼 시간이 오래 걸리는 작업을 처리할 때 **비동기 프로그래밍**을 사용합니다.  
초기에는 콜백 함수를 사용했지만, 코드가 복잡해지는 단점이 있었죠.  
이 문제를 해결하기 위해 나온 것이 **Promise**이고,  
Promise를 더 쉽고 깔끔하게 다루기 위한 문법이 바로 **async/await**입니다.

---

## ✅ async 키워드란?

- `async`는 함수 앞에 붙이는 키워드입니다.
- `async`가 붙은 함수는 **항상 Promise 객체를 반환**합니다.
- 함수 안에서 값을 리턴하면 자동으로 `Promise.resolve(리턴값)` 형태로 감싸집니다.

```js
async function example() {
  return 1; // Promise.resolve(1)
}
```

---

## ✅ await 키워드란?

- `await`은 **Promise가 처리될 때까지 기다리는 키워드**입니다.
- `await`은 오직 `async` 함수 내부에서만 사용할 수 있습니다.
- `await`이 붙은 코드는 해당 Promise가 완료될 때까지 함수의 실행을 일시 중단합니다.

```js
async function example() {
  let result = await someAsyncFunction();
  console.log(result);
}
```

---

## ✅ async/await 사용 예시

```js
const delay = (ms) => {
  return new Promise((resolve) => {
    setTimeout(() => resolve(`${ms}ms 후 완료`), ms);
  });
};

const run = async () => {
  console.log('시작');
  let result = await delay(3000);
  console.log(result);  // 3초 후에 출력
  console.log('끝');
};

run();
```
- 위 코드는 `await`를 통해 3초 동안 기다린 뒤 결과를 출력합니다.
- `then()`을 사용하지 않고도 비동기 처리를 깔끔하게 표현할 수 있죠.

---

## ✅ async/await의 장점

| 장점 | 설명 |
|:---|:---|
| 가독성 향상 | 복잡한 then 체인을 줄일 수 있어요. |
| 에러 핸들링 용이 | try...catch로 간단하게 예외 처리를 할 수 있어요. |
| 동기 코드처럼 작성 가능 | 비동기 처리를 직관적으로 이해할 수 있어요. |

---

## ✅ 추가로 알아두면 좋은 것: Promise.all

- 여러 개의 비동기 작업을 **동시에 실행**하고 모두 완료될 때까지 기다릴 때 사용합니다.

```js
const promise1 = delay(1000);
const promise2 = delay(2000);

const runAll = async () => {
  const results = await Promise.all([promise1, promise2]);
  console.log(results); // [ '1000ms 후 완료', '2000ms 후 완료' ]
};

runAll();
```

- 두 작업이 병렬로 진행되고, 둘 다 완료된 후 결과를 반환합니다.

---

# 🔥 마무리

`async/await`은 Promise를 더 쉽고, 직관적으로 다룰 수 있게 해주는 강력한 문법입니다.  
비동기 코드를 작성할 때 가독성과 유지보수성을 크게 향상시킬 수 있으니 꼭 알야아합니다.!!
