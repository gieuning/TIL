
# 자바스크립트의 this

자바스크립트에서 `this`는 함수가 호출된 방식에 따라 동적으로 바인딩되는 특별한 키워드입니다.
이 글에서는 `this`의 동작 방식을 여러 가지 예시를 통해 살펴보겠습니다.

## 일반 함수 호출

일반 함수로 호출된 `this`는 전역 객체를 가리킵니다.

```javascript
function func() {
    console.log(this); // 전역 객체 출력 (브라우저에서는 window 객체)
}

func(); // 일반 함수 호출
```

## 메서드로 호출

객체의 메서드로 호출된 `this`는 메서드를 포함하는 객체를 가리킵니다.

```javascript
const cafe = {
    brand: '이디야',
    menu: '아메리카노',
    print: function() {
        console.log(this);
    },
};

cafe.print(); // {brand: "이디야", menu: "아메리카노", print: function}
```

## 메서드 분리

메서드를 분리해서 호출한 경우 `this`는 전역 객체를 가리킵니다.

```javascript
const myCafe = cafe.print;
myCafe(); // 전역 객체 출력
```

## 생성자 함수

생성자 함수에서 `this`는 새로 생성된 객체를 가리킵니다.

```javascript
function Cafe(menu) {
    console.log(this);
    this.menu = menu;
}

let newCafe = new Cafe('latte');
let newCafe1 = Cafe('latte'); // 잘못된 호출 방식
console.log(newCafe); // {menu: 'latte'}
console.log(newCafe1); // undefined
```

## 콜백 함수에서 `this`

콜백 함수에서 `this`는 전역 객체를 가리키게 됩니다.

```javascript
const cafe1 = {
    brand: '이디야',
    menu: '',
    setMenu: function(menu) {
        this.menu = menu;
    }
}

function getMenu(menu, callback) {
    callback(menu); // 일반 함수 호출 -> window 객체 출력
}

getMenu('핫초코', cafe1.setMenu);
console.log(cafe1); // {brand: "이디야", menu: "핫초코"}
```

## `this`와 화살표 함수

화살표 함수는 자신을 포함하는 함수가 아닌, 함수가 선언된 위치의 `this`를 참조합니다.

```javascript
function newCounter() {
    this.count = 0;
    setInterval(() => { // 화살표 함수 사용
        this.count++;
        console.log(this.count);
    }, 2000);
}

const counter1 = new newCounter();
```

화살표 함수에서 `this`는 `newCounter` 함수 내에서 정의된 `this`를 참조합니다.

## 결론

- `this`는 함수의 호출 방식에 따라 값이 달라집니다.
- 화살표 함수는 `this`를 상위 스코프에서 가져오는 특성이 있습니다.

이러한 특성을 잘 활용하면 자바스크립트에서 `this`를 보다 효과적으로 다룰 수 있습니다.
