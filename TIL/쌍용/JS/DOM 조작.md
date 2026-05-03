## 📚 DOM이란?
> **DOM(Document Object Model)**은 HTML 문서를 브라우저가 객체로 변환한 구조입니다.
HTML 요소를 트리(Tree) 형태로 표현하며, 각각의 요소는 **노드(Node)**로 불립니다.
자바스크립트는 이 노드를 선택하고 수정하거나 삭제, 추가할 수 있습니다.

---

## 🧱 HTML, CSS, JavaScript의 역할

| 역할         | 설명                                         |
| ------------ | -------------------------------------------- |
| HTML         | 웹페이지의 구조를 정의                        |
| CSS          | 웹페이지를 꾸미는 역할                        |
| JavaScript   | 웹페이지를 동적으로 변화, 사용자와 상호작용 가능 |

---

## 🕵️‍♂️ DOM 요소 선택 방법

1. **getElementById()**  
   `id`를 통해 하나의 요소를 선택
   ```
   let $color = document.getElementById('color');
   ```
2. **querySelector()**  
   CSS 선택자 방식으로 처음 발견된 하나의 요소를 선택
   ```
   let ageElement = document.querySelector('div#age');
   ```
3. **querySelectorAll()**  
   CSS 선택자 방식으로 여러 요소를 선택 (NodeList 반환)
   ```
   let $infoItems = document.querySelectorAll('div.info-item');
   ```
4. **getElementsByClassName()**  
   특정 클래스명을 가진 모든 요소 선택 (HTMLCollection 반환)
   ```
   let $infoItems = document.getElementsByClassName('info-item');
   ```
5. **getElementsByTagName()**  
   특정 태그명을 가진 모든 요소 선택
   ```
   let $divs = document.getElementsByTagName('div');
   ```

---

## 🛠 DOM 요소 조작 방법

1. 클래스 변경
```js
let $name = document.getElementById('name');
$name.className = 'dog-name';
```
2. ID 변경
```js
let $animalInfo = document.querySelector('div.animal-info');
$animalInfo.id = 'animal';
```
3. 클래스 추가/삭제
```js
$color.classList.add('dog-color');
$color.classList.remove('info-item');
```
4. 스타일 변경
```js
$color.style.color = 'blue';
$color.style.fontSize = '30px';
```
5. 텍스트 수정
```js
let $age = document.getElementById('age');
$age.textContent = '5살';
```

---


## 🧩 DOM 깊은 조작 (Element 추가, 삭제, 교체, 복제)

**요소 생성과 추가**

```js
let $button = document.createElement('button');
$button.textContent = '클릭';
$animalInfo.appendChild($button);  // 요소 추가
```

**요소 삭제 (removeChild)**

```js
$animalInfo.removeChild($button);
```

**요소 교체 (replaceChild)**

```js
let $newElement = document.createElement('div');
$newElement.textContent = '새로운 요소';
$animalInfo.replaceChild($newElement, $button); // 기존 버튼을 새로운 div로 교체
```

**요소 삽입 (insertBefore)**
```js
let $anotherButton = document.createElement('button');
$anotherButton.textContent = '또 다른 버튼';
$animalInfo.insertBefore($anotherButton, $newElement);
```

**요소 복제 (cloneNode)**
```js
let $clone = $newElement.cloneNode(true); // true면 내부 자식 요소까지 복사
$animalInfo.appendChild($clone);

```
**인접 요소 삽입 (insertAdjacentElement)'beforebegin', 'afterbegin', 'beforeend', 'afterend' 위치 지정**

```js
$newElement.insertAdjacentElement('afterend', $clone);
```

**DocumentFragment 사용**
여러 요소를 한 번에 추가할 때 유용 (DOM 리플로우 최소화)

```js
let fragment = document.createDocumentFragment();
for (let i = 0; i < 3; i++) {
  let li = document.createElement('li');
  li.textContent = `아이템 ${i + 1}`;
  fragment.appendChild(li);
}
document.querySelector('ul').appendChild(fragment);
```

---

## 🛠 주요 DOM 조작 메서드 한눈에 보기

| 메서드               | 설명                         | 예시                             |
| -------------------- | ---------------------------- | -------------------------------- |
| createElement()       | 새 요소 생성                 | document.createElement('div')    |
| appendChild()         | 자식 요소 추가               | 부모.appendChild(자식)           |
| removeChild()         | 자식 요소 제거               | 부모.removeChild(자식)           |
| replaceChild()        | 자식 요소 교체               | 부모.replaceChild(새요소, 기존요소) |
| insertBefore()        | 특정 요소 앞에 삽입          | 부모.insertBefore(새요소, 기준요소) |
| cloneNode(true/false) | 요소 복제 (true: 자식까지 복제) | 요소.cloneNode(true)             |
| insertAdjacentElement() | 인접 위치에 삽입           | 요소.insertAdjacentElement('afterend', 새요소) |
| classList.add()       | 클래스 추가                 | 요소.classList.add('new-class')  |
| classList.remove()    | 클래스 제거                 | 요소.classList.remove('old-class') |
| textContent           | 텍스트만 조작               | 요소.textContent = '텍스트'      |
| innerHTML             | HTML 조작                  | 요소.innerHTML = '<b>텍스트</b>' |
| closest()             | 가장 가까운 조상 요소 찾기  | 요소.closest('div')              |

---

### 📌 innerHTML vs textContent vs innerText

| 속성         | 설명                                                   |
| ------------ | ------------------------------------------------------ |
| **innerHTML**   | HTML 형식의 텍스트 삽입 (HTML 태그 해석 가능)             |
| **textContent** | 순수 텍스트 삽입 (HTML 태그 해석 안함)                   |
| **innerText**   | 스타일이 적용된, 사용자에게 보이는 텍스트만 삽입/반환      |

주의: innerHTML은 보안(XSS 공격) 이슈가 있으니, 꼭 필요한 경우에만 사용하자.


## 🏁 마무리 요약

DOM은 HTML 문서를 자바스크립트가 조작할 수 있게 객체로 만든 구조.

다양한 메서드를 통해 원하는 요소를 선택, 생성, 삭제, 교체, 복제, 삽입할 수 있다.

많은 DOM 조작은 성능(리플로우, 리페인트)을 고려해 최적화해서 작성해야 한다.



