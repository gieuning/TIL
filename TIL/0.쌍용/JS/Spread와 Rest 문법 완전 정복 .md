
> 자바스크립트를 조금 더 깔끔하고 유연하게 다루고 싶다면 꼭 알아야 할 두 가지 문법이 있습니다. 바로 **Spread 문법**과 **Rest 문법**입니다. 둘 다 `...` 세 개의 점으로 표현되지만, **사용되는 위치에 따라 전혀 다른 역할**을 합니다.

---

## ✅ Spread 문법 (`...`)

### 📌 개념
- `Spread(펼치기)` 문법은 **배열이나 객체의 값들을 펼쳐서 복사하거나 병합할 때** 사용합니다.
- 배열 요소 또는 객체의 프로퍼티를 하나하나 분해하여 새로운 배열이나 객체에 포함시킬 수 있습니다.

### 🧸 객체에서 Spread 사용 예제

```js
const toy = {
  type: 'bear',
  price: 15000
};

const yellowToy = {
  ...toy,
  color: 'yellow'
};

console.log(yellowToy);
// 출력: { type: 'bear', price: 15000, color: 'yellow' }
```

- `...toy`는 `toy` 객체의 모든 프로퍼티를 펼쳐서 새로운 객체에 복사합니다.

### 🌈 배열에서 Spread 사용 예제

```js
const color1 = ['red', 'orange', 'yellow'];
const color2 = ['blue', 'navy', 'purple'];

const rainbow = [...color1, 'green', ...color2];
console.log(rainbow);
// 출력: ['red', 'orange', 'yellow', 'green', 'blue', 'navy', 'purple']
```

- 배열 사이에도 자유롭게 삽입하거나 결합할 수 있습니다.
- 순서에 상관 없이 여러 번 사용 가능.

### 🎁 함수 인자에서 Spread 사용

```js
const numbers = [1, 2, 3, 4, 5];
console.log(...numbers); // 1 2 3 4 5
```

- 함수에 배열을 각각의 인자로 전달하고 싶을 때 매우 유용합니다.

---

## ✅ Rest 문법 (`...`)

### 📌 개념
- `Rest(나머지)` 문법은 **여러 값을 하나의 배열이나 객체로 묶을 때** 사용합니다.
- **함수 매개변수**나 **객체/배열의 구조분해 할당**에서 주로 사용됩니다.
- 항상 마지막에 위치해야 하며, 여러 번 쓸 수 없습니다.

### 🧸 객체 구조 분해 + Rest

```js
const blueToy = {
  type: 'bear',
  price: 15000,
  color: 'blue'
};

const { type, ...rest } = blueToy;

console.log(type); // 'bear'
console.log(rest); // { price: 15000, color: 'blue' }
```

- `type`을 제외한 나머지 프로퍼티들을 `rest` 객체에 담습니다.

### 👨‍💻 함수 매개변수에서 Rest

```js
const printRest = (a, b, ...rest) => {
  console.log([a, b, rest]);
};

printRest(1, 2, 3, 4, 5, 6);
// 출력: [1, 2, [3, 4, 5, 6]]
```

- 첫 두 개는 각각 `a`, `b`에 들어가고 나머지는 모두 `rest` 배열에 들어갑니다.

### 🔧 rest + 배열 전체 받기

```js
const printNumbers = (...rest) => {
  console.log(rest);
};

printNumbers(1, 2, 3, 4, 5);
// 출력: [1, 2, 3, 4, 5]
```

- 매개변수 개수를 알 수 없을 때 간편하게 모두 받을 수 있습니다.

---

## 📝 Spread vs Rest 요약

| 구분 | 문법 | 사용 위치 | 역할 |
|------|------|------------|------|
| Spread | `...` | 함수 호출, 객체/배열 리터럴 내 | 값 *펼치기* (복사/병합 등) |
| Rest | `...` | 함수 매개변수, 구조분해 할당 | 값 *모으기* (배열/객체로 묶음) |
