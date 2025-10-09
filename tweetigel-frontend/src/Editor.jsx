import {useContext, useEffect, useRef, useState} from "react";
import {API} from "./Context.js";
import {contentTypeJson} from "./RequestHeaders.js";

function Editor({endpoint, type, onSend}){
    const api = useContext(API)
    const content = useRef(undefined)
    const [sent, setSent] = useState(false)
    const [isLengthValid, setIsLengthValid] = useState(true)

    useEffect(() => {
        setSent(false)
        setIsLengthValid(true)
    }, [])

    function send(){
        event.preventDefault();
        if(content.current.value === "" || content.current.value===undefined){
            alert("Post can't be empty!")
            return
        }
        if(content.current.value.length > 140) {
            setIsLengthValid(!isLengthValid);
            setTimeout(() => setIsLengthValid(true), 3000);
            return;
        }
        const payload = {content:content.current.value}
        fetch(`${api}${endpoint}`, { // Interpolation Required
            method:'POST',
            headers: contentTypeJson(),
            body: JSON.stringify(payload),
            credentials: 'include'
        }).then(response => {
                if(!response.ok){
                    alert(`Failed to send ${type}.`);
                }else{
                    content.current.value=""
                    setSent(true)
                    setTimeout(() => setSent(false), 3000);
                }
        }).then(() => onSend())
    }

    return <>
            <div className="editor-container">
                {sent === true?
                    <ul>
                         <textarea
                             name="box"
                             aria-invalid="false"
                             aria-describedby="box-helper"
                             placeholder="Enter content here." ref={content}></textarea>
                            <small id="box-helper">Success!</small>
                    </ul>
                    : !isLengthValid ?
                            <ul>
                              <textarea
                                  name="box"
                                  aria-invalid="true"
                                  aria-describedby="invalid-helper"
                                  placeholder="Enter content here."
                                  ref={content}></textarea>
                                <small id="invalid-helper">Content exceeds max length of 140 characters!</small>
                            </ul>
                        :  <ul>
                            <textarea placeholder="Enter content here." ref={content}></textarea>
                        </ul>
                }
                <ul>
                    <button className="pico-background-jade-350" onClick={send}>{type}</button>
                </ul>
            </div>
        </>
}

export default Editor