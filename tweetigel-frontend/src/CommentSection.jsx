import {useContext, useState} from "react";
import Editor from "./Editor.jsx";
import {API} from "./Context.js";

function CommentSection({commentsCount, postId, onCommentAdded}){
    const api = useContext(API)
    const [comments, setComments] = useState([]);
    const [showEditor, setShowEditor] = useState(false);

    function getComments(){
        fetch(api+"/post/" + postId + "/comments", {
            method: 'GET'
        }).then(response => {
                if (!response.ok) {
                    alert("Unable to get comments: " + response.statusText)
                    return []
                } else {
                    return response.json()
                }
            }).then(parsedResponse => {
                setComments(parsedResponse);
        })
    }

    function update(){
        getComments()
        if(onCommentAdded !== null) onCommentAdded()
    }

    function displayEditor(){
        setShowEditor(!showEditor)
    }

    return <>
        <details onToggle={getComments}>
            <summary>
                <u>{commentsCount} Comment{commentsCount>1?"s":""}</u>
            </summary>
            <hr />
            <button className="outline" onClick={displayEditor}>Leave a Comment {showEditor? "↓" :"→"}</button>
            {showEditor === true?
                <div className="flex">
                    <Editor endpoint={`/post/${postId}/comment`} type={`Comment`} onSend={update}></Editor>
                </div>
                : <></>
            }
            <br /><br />
            {comments.length === 0?
                <p>No comments yet.</p>
                :
                <ul>
                    {comments.map(comment => (
                        <p key={comment.id}>
                            <kbd>@{comment.author}</kbd>&nbsp;&nbsp;&nbsp;<sub><i>{comment.timestamp}</i></sub>
                            <br/><br/>
                            {comment.content}
                        </p>
                    ))}
                </ul>
            }
        </details>
    </>
}

export default CommentSection