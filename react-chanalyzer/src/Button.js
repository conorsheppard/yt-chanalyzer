const Button = ({ text, loading }) => {
    return (
      <button className="submit-button" type="submit">
        {!loading ? text : 'loading...'}
      </button>
    )
  }
  
  export default Button