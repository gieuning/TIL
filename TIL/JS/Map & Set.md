# 📦 JavaScript의 `Map`과 `Set` 완벽 정리

> 자바스크립트에는 객체(`Object`)나 배열(`Array`) 외에도 특별한 목적에 맞게 사용할 수 있는 자료구조인 `Map`과 `Set`이 존재합니다.

## 🔑 `Map`이란?

- **키-값 쌍(key-value pair)** 으로 데이터를 저장
- 객체와 달리 **모든 자료형**을 키로 사용할 수 있음
- 키의 순서를 **삽입된 순서대로** 유지함
- 주로 키-값 구조가 필요한 경우 `Object`보다 **더 정밀하고 명확하게 사용 가능**

### ✅ Map 주요 메서드

| 메서드 | 설명 |
|--------|------|
| `set(key, value)` | 키에 해당하는 값을 설정 |
| `get(key)` | 특정 키의 값을 반환 |
| `has(key)` | 특정 키가 존재하는지 확인 |
| `delete(key)` | 특정 키 삭제 |
| `clear()` | 모든 요소 삭제 |
| `size` | 요소의 개수 반환 |

### ✅ 예시

```js
const map = new Map();
map.set('name', 'gieun');
map.set(1, 'one');
map.set(true, 'yes');

console.log(map.get('name')); // 'gieun'
console.log(map.has(1)); // true
console.log(map.size); // 3
```

> 📌 객체보다 더 명확하게 키를 다루고 싶을 때는 `Map`을 사용
---

## 🧺 `Set`이란?

- **중복되지 않는 유일한 값들의 집합**
- 배열과 유사하지만 중복을 허용하지 않음
- 삽입 순서를 기억함
- 주로 **중복 제거**, **유일값 저장** 등에서 활용

### ✅ Set 주요 메서드

| 메서드 | 설명 |
|--------|------|
| `add(value)` | 값을 추가 |
| `has(value)` | 값 존재 여부 확인 |
| `delete(value)` | 값 제거 |
| `clear()` | 모든 값 제거 |
| `size` | 요소 개수 반환 |

### ✅ 예시

```js
const s = new Set();
s.add(1);
s.add(1);
s.add(2);

console.log(s); // Set(2) {1, 2}
console.log(s.has(1)); // true

for (const item of s) {
    console.log(item);
}

s.delete(2);
s.clear();
```

### ✅ 배열에서 중복 제거하기

```js
const arr = [1, 2, 2, 3, 3, 5];
const unique = Array.from(new Set(arr));
console.log(unique); // [1, 2, 3, 5]
```

---

## 🔍 Map vs Set vs Object vs Array

| 항목       | Map         | Set        | Object     | Array      |
|------------|-------------|------------|------------|------------|
| 키         | 모든 자료형 | 없음       | 문자열/심벌 | 없음       |
| 값         | O           | O          | O          | O          |
| 순서 보존  | O           | O          | X (ES6부터 일부 보존) | O |
| 중복 허용  | 키 중복 X   | 값 중복 X  | 키 중복 X  | 값 중복 O  |
| 주 사용 목적 | 키-값 저장 | 중복 없는 값 저장 | 일반 데이터 저장 | 리스트 처리 등 |

---

## ✨ 언제 어떤 걸 써야 할까?

- 키-값 형태가 필요하다면? → `Map`
- 중복 제거가 필요하다면? → `Set`
- 단순히 구조화된 데이터 저장이 목적이라면? → `Object` 또는 `Array`