# 2025-03-10
---

# 오늘 공부 내용

---

# Hooks
클래스형 컴포넌트에서만 가능했던 기능(상태 관리, 라이프사이클 메서드 등)을 함수형 컴포넌트에서도
사용할 수 있도록 하는 기능

과거에는 함수형 컴포넌트에서 상태 관리나 라이프사이클 기능을 사용할 수 없었다.
하지만 훅이 도입되면서 함수형 컴포넌트에서도 상태를 관리하고, 부작용(effect)을 처리할 수 있게 되었다.


**장점**
- 클래스 없이 상태(state)와 라이프사이클을 관리할 수 있음
- 코드가 간결하고 유지보수가 쉽다.
- 재사용성이 높다.(커스텀 Hook 생성 가능)

## ** 대표적인 리액트 훅**

| **훅 (Hook)**    | **설명** |
|-----------------|---------|
| `useState`      | 상태(state) 관리 |
| `useEffect`     | 컴포넌트의 라이프사이클 관리 (비동기 작업, API 호출, 이벤트 핸들링 등) |
| `useRef`        | DOM 요소 접근 및 렌더링 없이 값 유지 |
| `useContext`    | 전역 상태 관리 (Context API 활용) |
| `useReducer`    | 복잡한 상태 관리 (Redux 대체 가능) |
| `useMemo`       | 값의 캐싱 (렌더링 성능 최적화) |
| `useCallback`   | 함수의 캐싱 (불필요한 렌더링 방지) |
| `useLayoutEffect` | `useEffect`와 유사하지만, 렌더링 직후 동기적으로 실행 |
| `useImperativeHandle` | `ref`를 활용하여 부모 컴포넌트가 자식 컴포넌트의 특정 메서드 호출 가능 |
| `useId`         | 고유한 ID 생성 (접근성, 리스트 키 필요할 때) |


## useEffect
- 컴포넌트가 렌더링된 후 특정 로직을 실행하도록 도와주는 훅
- 비동기 데이터 가져오기, DOM 조작, 이벤트 리스너 추가 및 정리 등 다양한 작업을 수행
```javascript
useEffect(() => {
  // 실행할 코드
}, [의존성 배열]);
```
- 첫 번째 인자: 실행할 함수
- 두 번째 인자(의존성 배열): 어떤 값이 변경될 때 실행할지 결정
    - 의존성 배열이 없을 경우 매 렌더링마다 실행이 된다.

**useEffect 실행 시점**
- 컴포넌트가 마운트(처음 나타날 때) 실행
```javascript
import { useEffect } from "react";

function EffectEx() {
  useEffect(() => {
      console.log('마운트 될때만 실행 !!!');
  }, []); // 빈 배열 → 처음 렌더링 시에만 실행

  return <div>방가방가</div>;
}

export default EffectEx;
```

- 특정 상태(state)나 프롭(props)이 변경될 때 실행
```javascript
import { useState, useEffect } from "react";

function EffectEx2() {
    const [count, setCount] = useState(0);

    useEffect(() => {
        console.log(`🔄 카운트 변경됨: ${count}`);
    }, [count]); // count 값이 바뀔 때마다 실행

    return (
        <div>
            <p>카운트: {count}</p>
            <button onClick={() => setCount(count + 1)}>+1 증가</button>
        </div>
    );
}

export default EffectEx2;
```

- 언마운트(컴포넌트가 사라질 때)시 실행
```javascript
import { useEffect, useState } from "react";

function Timer() {
  useEffect(() => {
    const interval = setInterval(() => {
      console.log("1초마다 실행");
    }, 1000);

    return () => {
      clearInterval(interval); // 컴포넌트가 사라질 때 정리
      console.log("타이머 정리");
    };
  }, []);

  return <div>타이머 실행 중...</div>;
}

export default Timer;
```

## useRef
- DOM 요소에 직접 접근하거나 컴포넌트의 값을 유지하는데 사용
    - 값이 변경되어도 컴포넌트가 다시 렌더링되지 않는다는 특징이 있다.
      : 래퍼런스를 관리하는데 사용되는 훅
      DOM 요소에 대한 접근을 제공하거나,
      값이 변해도 리렌더링을 원하지 않는 경우에아 숑
      : current 속성
      - 저장된 객체를 접근할때 사용하는 속성

## useCallback()
- 성능 최적화를 위해 사용하는 훅
- 주로 함수가 리랜더링될때마다 생성되는 것을 방지하고 함수가 특정 의존성 값이 변경될때만 새로 생성되도록 할 수 있다.
- 불필요한 렌더링을 방지하여 성능 최적화
- 두번째 인자
- 의존성 배열. 빈배열은 컴포넌트가 마운트될때 하한번 실행
```javascript

```

## useMemo()
- 성능 최적화를 위해 특정 값을 메모이제이션(캐싱)할 때 사용
- 계산이 비싼 작업이나 반복되는 계산을 최적화하는데 유용
- 특정 값이나 연산을 의존성 배열의 값이 변경될때만 다시 계산하고 그외는 이전에 계산된 값을 재사용 




## @CrossOrigin 
- 모든 도메인, 모든 요청방식에 대해 허용 한다는 뜻
CORS(Cross-origin resource sharing)란 ?
- 웹 페이지의 제한된 자원을 외부 도메인에서 접근을 허용해주는 메커니즘
- CROS를 스프링을 통해 설정할 수 있는 기능


- 지정 사이트에서 AJAX 요청 허용



- Rest ful 가지고 모함?
- 