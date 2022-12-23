import React, {useReducer, useEffect, useState} from "react";
import Select from 'react-select';

const POPULATE_STATE = 'populateState'
const CLEAR = 'clear'
const POPULATE_CITY = 'populateCity'


// const [selectedCountry, setSelectedCountry] = useState([]);
// const [selectedState, setSelectedState] = useState([]);
// const [selectedCity, setSelectedCity] = useState([]);

//https://mocki.io/v1/ef136b09-7932-4bff-878e-f56b5e420806


const data = {
    countries: [
        {
            value: 'INDIA',
            label: 'India',
            states: [
                {
                    value: 'TAMIL NADU', label: 'Tamil Nadu',
                    cities: [
                        {value: 'Chennai', label: 'Chennai'},
                        {value: 'Trichi', label: 'Trichi'}
                    ]
                },
                {
                    value: 'KERALA', label: 'Kerala',
                    cities: [
                        {value: 'Trivandrum', label: 'Trivandrum'},
                        {value: 'Kochi', label: 'Kochi'}
                    ]
                },
                {
                    value: 'ANDHRA PRADESH', label: 'Andhra Pradesh',
                    cities: [
                        {value: 'Vijaywada', label: 'Vijaywada'},
                        {value: 'Vizag', label: 'Vizag'}
                    ]
                }
            ]
        },
        {
            value: 'US',
            label: 'USA',
            states: [
                {value: 'CA', label: 'California'},
                {value: 'NY', label: 'New York'}
            ]
        }
    ]
}

const initialState = {
    disableCountry: false,
    disableState: true,
    loadingState: false,
    disableCity: true,
    loadingCity: false,
    statesToBeLoaded: [],
    citiesToBeLoaded: []
}


// const API = 'https://mocki.io/v1/ef136b09-7932-4bff-878e-f56b5e420806';
// const fetchPost = () => {
//     fetch(API)
//         .then((res) => res.json())
//         .then((res) => {
//             console.log(res)
//             getPost(res)
//         })
// }
// useEffect(() => {
//     fetchPost()
// }, [])


function reducer(state, action) {
    switch (action.type) {
        case POPULATE_STATE:
            return {
                ...state,
                disableCountry: true,
                disableState: false,
                loadingState: true,
                statesToBeLoaded: data.countries.find(
                    country => country.value === action.country
                ).states
            }
        case POPULATE_CITY:
            return {
                ...state,
                disableState: false,
                disableCity: false,
                loadingCity: true,

                citiesToBeLoaded: data.countries.find(() => countrys)

                    .states.find(state => state.value === action.state).cities
            }
        case CLEAR:
        default:
            return initialState
    }
}

let countrys = '';

// function* callAPI(){
//     return yield call(fetch, 'https://mocki.io/v1/ef136b09-7932-4bff-878e-f56b5e420806');
// }


// const fetchdata = () => {
//     const options = {
//         method: `GET`,
//     };
//     return fetch(`https://mocki.io/v1/ef136b09-7932-4bff-878e-f56b5e420806`, options)
//         .then((response) => {
//             if(response.ok){
//                 return response.json()
//             }
//             throw new Error('Api is not available')
//         })
//         .catch(error => {
//             console.error('Error fetching data ', error)
//         })
// }
// var selectedCountry;
// var selectedState;
function Nestedselect() {
    //const countrys = useRef(0);

    const [post, setPost] = useState([]);
    const API = 'https://mocki.io/v1/731dfc98-b3cb-483b-b206-aaa7d0211cc7';
    const fetchPost = () => {
        fetch(API)
            .then((res) => res.json())
            .then((res) => {
                // console.log(res)
                // const data1 =res;
                //
                // setPost(post => data1.countries)
                setPost(res.countries);
                data.countries = res.countries;
                console.log('post',{post})
                console.log('data',data.countries)
                //console.log({post.map(obj =>(data.countries=obj))})
            })
    }
    useEffect(() => {
        fetchPost()
    }, [])

    // const temp=function fctchapis(){
    //     fetch('https://mocki.io/v1/ef136b09-7932-4bff-878e-f56b5e420806')
    //         .then((res) =>{
    //             return res.json()
    //         })
    //         .then((jsondata) => {
    //             console.log(jsondata)
    //         })
    // }

    const handleOnCountryClick = event => {
        console.log(event.value);

        //      console.log(countrys.current);
        //      selectedCountry = event.value;
        //    countrys.current=event.value;
        //      console.log("select" + selectedCountry);
        countrys = event.value;
        console.log(countrys);

        dispatch({type: POPULATE_STATE, country: event.value})
    };
    const handleOnStateClick = event => {
        console.log(event.value);
        //       selectedState = event.value;
        //      console.log("select" + selectedState);
        dispatch({type: POPULATE_CITY, state: event.value})
    };

    const [state, dispatch] = useReducer(reducer, initialState)
    return (
        <div>
            <Select
                isDisabled={state.disableCountry}
                isLoading={state.loadingState}
                isClearable
                isSearchable
                placeholder="Select Country..."
                name="country"
                options={data.countries}
                onChange={handleOnCountryClick}
            />
            <br/>
            {!state.disableState && (
                <>
                    <Select
                        isDisabled={state.disableState}
                        isLoading={state.loadingCity}
                        isClearable
                        isSearchable
                        placeholder="Select State..."
                        name="state"
                        options={state.statesToBeLoaded}
                        onChange={handleOnStateClick}
                    />
                    <br/>
                </>
            )}

            {!state.disableCity && (
                <>
                    <Select
                        isDisabled={state.disableCity}
                        isLoading={false}
                        isClearable
                        isSearchable
                        placeholder="Select City..."
                        name="city"
                        options={state.citiesToBeLoaded}
                    />
                    <br/>
                </>
            )}
        </div>
    )

}

export default Nestedselect;