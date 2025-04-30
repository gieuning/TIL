# JavaScript에서 `falsy`, `??`, `||`, `?.` 연산자 개념 정리

> JavaScript에서는 값의 존재 여부를 확인하거나, 안전하게 프로퍼티를 접근하기 위해 다양한 연산자를 제공합니다. 이번 포스팅에서는 자주 사용되는 연산자들인 `||`, `??`, 그리고 `?.` (옵셔널 체이닝 연산자)에 대해 설명하고, 각 연산자의 차이점과 사용법을 정리해 보겠습니다.

---

## 🎯 falsy 값이란?

JavaScript에서 조건문 등에서 `false`로 간주되는 값들을 **falsy** 값이라고 합니다. 다음 값들이 이에 해당합니다:

- `false`
- `0`
- `''` (빈 문자열)
- `null`
- `undefined`
- `NaN`

그 외의 값들은 모두 **truthy**로 간주됩니다.

---

## `||` 연산자 - OR (논리합)

`||` 연산자는 **왼쪽 값이 falsy일 경우에만 오른쪽 값을 반환**합니다. 그래서 기본값 설정에 많이 사용됩니다.

```js
const a = 0;
const b = a || 3;
console.log(b); // 3 (0은 falsy이므로 3 반환)
```

### 문제점 ❗
`||`는 `0`, `''`, `false`도 falsy로 보기 때문에, 이 값들을 정상적인 데이터로 다뤄야 할 경우에는 문제가 발생할 수 있습니다.

---

## `??` 연산자 - Null 병합 연산자

`??` 연산자는 **왼쪽 값이 `null` 또는 `undefined`일 때만 오른쪽 값을 반환**합니다.

```js
const c = 0;
const d = c ?? 3;
console.log(d); // 0 (null, undefined가 아니므로 그대로 반환)

const e = null;
const f = e ?? 3;
console.log(f); // 3

const g = undefined;
const h = g ?? 3;
console.log(h); // 3
```

### ✅ 추천 사용 케이스
- `0`, `false`, `''`을 **정상적인 값**으로 판단해야 할 때
- API 응답값에서 누락된 값만 처리하고 싶을 때

---

## `?.` 연산자 - 옵셔널 체이닝 (Optional Chaining)

`?.` 연산자는 **왼쪽 객체가 `null` 또는 `undefined`인 경우 오류를 발생시키지 않고 `undefined`를 반환**합니다. 안전하게 속성에 접근할 수 있습니다.

```js
const obj = { name: "John" };
console.log(obj.name); // "John"

const nullObj = null;
console.log(nullObj?.name); // undefined (오류 없음)

const user = null;
console.log(user?.getProfile?.()); // undefined (getProfile이 없으므로 안전하게 반환)
```

### ⚠️ 일반적인 접근의 위험성
```js
const user = null;
console.log(user.name); 
// TypeError: Cannot read properties of null (reading 'name')
```

---

## 📌 요약 비교표

| 연산자 | 동작 조건 | 반환 값 | 주의사항 |
|--------|------------|---------|-----------|
| `||`   | 왼쪽이 falsy이면 | 오른쪽 반환 | `0`, `''`, `false`도 falsy로 처리 |
| `??`   | 왼쪽이 `null` 또는 `undefined`이면 | 오른쪽 반환 | falsy 값들은 유지됨 |
| `?.`   | 왼쪽이 `null` 또는 `undefined`이면 | `undefined` 반환 | 중첩된 프로퍼티 접근에 유용 |

---

## 🔚 마무리

- `||`는 일반적으로 기본값을 설정할 때 많이 사용되지만, 모든 falsy 값을 체크하므로 상황에 따라 부적절할 수 있습니다.
- `??`는 `null`과 `undefined`만을 체크하므로 보다 정밀한 기본값 설정에 적합합니다.
- `?.`는 안전하게 객체의 속성을 접근할 때 매우 유용하며, 예외 발생을 방지할 수 있습니다.

이 세 가지 연산자는 자바스크립트의 가독성과 안정성을 크게 높여주는 기능들이므로, 올바르게 이해하고 상황에 맞게 사용하는 것이 중요합니다.