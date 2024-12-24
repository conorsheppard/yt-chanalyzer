import { useState } from "react";
import Button from './Button';
import "./FormInput.css";

const FormInput = (props) => {
  const [focused, setFocused] = useState(false);
  const { label, onChange, onSubmit, onInvalid, onInput, loading, placeholder, id, ...inputProps } = props;

  const handleFocus = (e) => {
    setFocused(true);
  };

  return (
    <div className="FormInput">
        <div className="search-bar-and-graph">
            <div className="search-bar-form-and-text">
            <form onSubmit={onSubmit}>
                <div className="search-bar-text">Enter a YouTube channel name</div>
                <div className="search-bar-elements">
                    <div className="search-bar-prefix-link">https://www.youtube.com/</div>
                    <input
                        className="search-bar-input"
                        {...inputProps}
                        onChange={onChange}
                        onBlur={handleFocus}
                        focused={focused.toString()}
                        placeholder={placeholder}
                        onInvalid={onInvalid}
                        onInput={onInput}
                    />
                    <Button text="Submit" loading={loading} />
                </div>
            </form>
            </div>
        </div>
    </div>
  );
};

export default FormInput;