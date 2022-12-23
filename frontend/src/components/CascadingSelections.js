import React, {useEffect, useState} from "react";
import Form from "@awsui/components-react/form";
import FormField from "@awsui/components-react/form-field";
import Input from "@awsui/components-react/input";
import Select, {SelectProps} from "@awsui/components-react/select";
import Container from "@awsui/components-react/container";
import Header from "@awsui/components-react/header";
import SpaceBetween from "@awsui/components-react/space-between";
import Button from "@awsui/components-react/button";
import AppLayout from "@awsui/components-react/app-layout";

function CascadingSelections() {

    console.log("CascadingSelections called ...1");
    const countriesList = [
        {label: "India", id:"1",value: "India"},
        {label: "USA", id:"2",value: "USA"},
    ];

    let statesList = [
        {label: "Bengal", id:"1",countryId: "1", value: "Bengal"},
        {label: "Telengana", id:"2",countryId: "1", value: "Telengana"},
        {label: "Texas", id:"3",countryId: "2", value: "Texas"},
        {label: "Georgia", id:"4", countryId: "2", value: "Georgia"},
    ];

    const citiesList = [
        {label: "Kolkata", stateId: "1", name: "Kolkata"},
        {label: "Durgapur", stateId: "1", name: "Durgapur"},
        {label: "Hyderabad", stateId: "2", name: "Hyderabad"},
        {label: "Dallas", stateId: "3", name: "Dallas"},
        {label: "Atlanta", stateId: "4", name: "Atlanta"},
    ];

    const [country, setCountry] = useState("USA");
    const [state, setState] = useState([]);
    const [city, setCity] = useState([]);

    useEffect(() => {
        setCountry(countriesList);
    }, []);

    const handleCountryChange = (countryval) => {
        console.log("handleCountryChange called");
        console.log("Event is ", countryval);
        setCountry(country => countryval);
        console.log("country val"+countryval.value);
        const filteredStates = statesList.filter(
            (eachState) => eachState.countryId === countryval.id
        );
        //console.log(filteredStates);
       statesList = filteredStates;
        // this.setState({ setStates: filteredStates })
        //setState(filteredStates);
        setState(state => filteredStates);
        //setCity([]);
    };
    //Source Tech Stack,

    const handleStateChange = (stateval) => {
        console.log("handleStateChange called");
        //console.log("Event is ", e.detail.selectedOption.id);
        //setState(state => stateval);
        console.log("stateval"+stateval.value)
        console.log("state"+state)
        const filteredCities = citiesList.filter(
            (eachCity) => eachCity.stateId === stateval.id
        );
        setCity(city => filteredCities);
    };

    const handleCityChange = (e) => {
        console.log("handleCityChange called");
        // let p1 = document.getElementById("e_country").value;
        // let p2 = document.getElementById("e_state").value;
        // let p3 = document.getElementById("e_city").value;
        //console.log(p1, "--", p2, "--", p3);
    };

    return (
        <AppLayout content={
            <Form>
                <Container>
                    <SpaceBetween direction="vertical" size="l">
                        <FormField label="Country" description="Country details" >
                            <Select options={countriesList}
                                    onChange={(event) => handleCountryChange(event.detail.selectedOption)}
                                    selectedAriaLabel="selected" selectedOption={country}
                                    //onChange={(event) => setCountry(event.detail.selectedOption)}
                            />
                        </FormField>
                        {state && state !== undefined && state.length > 0 ? (
                            <FormField label="State" description="State details" >
                                <Select options={state}
                                        onChange={(event) => handleStateChange(event.detail.selectedOption)}
                                        selectedAriaLabel="selected" selectedOption={state}
                                />
                            </FormField>
                            ):null}
                        {city && city !== undefined && city.length > 0  ? (
                            <FormField label="City" description="City details">
                                <Select options={city}
                                        onChange={(event) => handleCityChange(event)}
                                        selectedAriaLabel="selected"
                                />
                            </FormField>
                        ):null}
                    </SpaceBetween>
                </Container>
            </Form>
        }
        />
    );
}

export default CascadingSelections;
