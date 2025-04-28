```js
let $type = document.createElement('div');
$type.className = 'info-item';
$type.id = 'type';
$type.textContent = '말티즈';

let $typeText = document.l('말티즈');

// 지금까지 생성한 노드들은 단지 생성만 되었을 뿐 아직 DOM 트리에 추가된 것이 아님
// 생성한 노드들을 DOM 트리에 추가해서 화면에 나타내자

// appendChild 라는 메서드를 사용해서 전달받은 노드를 원하는 요소의 마지막에 가능
// dom 트리는 노드 간의 상하관계를 한눈에 볼 수 있는 트리 구조
// 부모 노드를 작성한 후 appendchild 괄호 안에는 부모 노드에 새로 추가할 자식 노드를 추가
let $animalInfo = document.querySelector('div.animal-info');
$animalInfo.appendChild($type);
$type.appendChild($typeText);


console.log($type);
console.log($typeText);




innerHTML - DOP API 속성 중 하나로 특정 요소의 HTMl 문자열 형태로 읽거나 설정 할 수 있다. 
    내부에 있는 모든 html 요소들이 출력 
/*

$type.textContent = '말티즈';
$type.innerHTML ='말티즈'
$type.innerText='말티즈';*/

```

| 속성  | 설명                | 사용 예시 |
|-----|-------------------|-------|
| for | 연결할 입력 요소의 id를 지정 |    <label for="username">아이디</label>|

![img.png](img.png)
